package com.waltercraft.auth.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waltercraft.auth.config.MicrosoftOAuthProperties;
import com.waltercraft.auth.dto.DeviceCodeResponse;
import com.waltercraft.auth.dto.MinecraftProfile;
import com.waltercraft.auth.exception.MinecraftOAuthException;
import com.waltercraft.auth.exception.MinecraftProfileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class MinecraftOAuthClient {

    private static final Logger log = LoggerFactory.getLogger(MinecraftOAuthClient.class);
    private static final String USER_AGENT = "WaltercraftAuthService/1.0 (+https://waltercraft.ru)";

    private static final String MS_AUTH_URL = "https://login.live.com/oauth20_authorize.srf";
    private static final String MS_TOKEN_URL = "https://login.live.com/oauth20_token.srf";
    private static final String MS_DEVICE_CODE_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";
    private static final String MS_V2_TOKEN_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    private static final String XBL_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String MINECRAFT_LOGIN_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String MINECRAFT_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    private final MicrosoftOAuthProperties properties;
    private final RestClient restClient;

    public MinecraftOAuthClient(MicrosoftOAuthProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
    }

    public String buildAuthorizeUrl(String customRedirectUri, String state) {
        String effectiveRedirect = resolveRedirectUri(customRedirectUri);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(MS_AUTH_URL)
                .queryParam("client_id", properties.clientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", effectiveRedirect)
                .queryParam("scope", properties.scopes())
                .queryParam("prompt", "select_account");

        if (state != null && !state.isBlank()) {
            builder.queryParam("state", state);
        }
        return builder.toUriString();
    }

    public DeviceCodeResponse initiateDeviceCodeFlow() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", properties.deviceClientId());
        formData.add("scope", "XboxLive.signin offline_access");

        try {
            MsDeviceCodeResponse response = restClient.post()
                    .uri(MS_DEVICE_CODE_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(MsDeviceCodeResponse.class);

            if (response == null || response.deviceCode() == null) {
                throw new MinecraftOAuthException("Пустой ответ от Microsoft Device Code сервиса");
            }

            return new DeviceCodeResponse(
                    response.deviceCode(),
                    response.userCode(),
                    response.verificationUri(),
                    response.expiresIn() != null ? response.expiresIn() : 900,
                    response.interval() != null ? response.interval() : 5
            );
        } catch (RestClientResponseException ex) {
            log.error("Device code request failed with status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MinecraftOAuthException(extractErrorMessage(ex, "Ошибка получения кода устройства Microsoft"), ex);
        } catch (Exception ex) {
            log.error("Unexpected error initiating device code flow", ex);
            throw new MinecraftOAuthException("Не удалось связаться с Microsoft сервисом авторизации", ex);
        }
    }

    public Optional<String> pollDeviceToken(String deviceCode) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
        formData.add("client_id", properties.deviceClientId());
        formData.add("device_code", deviceCode);

        try {
            MsTokenResponse response = restClient.post()
                    .uri(MS_V2_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(MsTokenResponse.class);

            if (response != null && response.accessToken() != null) {
                return Optional.of(response.accessToken());
            }
            return Optional.empty();
        } catch (RestClientResponseException ex) {
            String body = ex.getResponseBodyAsString();
            if (body != null) {
                if (body.contains("authorization_pending") || body.contains("slow_down")) {
                    return Optional.empty();
                }
                if (body.contains("expired_token")) {
                    throw new MinecraftOAuthException("Срок действия кода авторизации истёк. Запросите новый код.");
                }
            }
            log.error("Device token poll failed with status {}: {}", ex.getStatusCode(), body);
            throw new MinecraftOAuthException(extractErrorMessage(ex, "Ошибка проверки статуса авторизации"), ex);
        } catch (Exception ex) {
            log.error("Unexpected error polling device token", ex);
            throw new MinecraftOAuthException("Не удалось проверить статус авторизации Microsoft", ex);
        }
    }

    public MinecraftProfile exchangeMsAccessTokenForMinecraftProfile(String msAccessToken) {
        XboxToken xblToken = acquireXboxLiveToken(msAccessToken);
        XboxToken xstsMojangToken = acquireXstsToken(xblToken.token(), "rp://api.minecraftservices.com/");
        String minecraftToken = acquireMinecraftAccessToken(xstsMojangToken.userHash(), xstsMojangToken.token());
        return fetchMinecraftProfile(minecraftToken);
    }

    public MinecraftProfile exchangeCodeForMinecraftProfile(String code, String customRedirectUri) {
        String effectiveRedirect = resolveRedirectUri(customRedirectUri);

        String msAccessToken = acquireMicrosoftAccessToken(code, effectiveRedirect);
        XboxToken xblToken = acquireXboxLiveToken(msAccessToken);

        if (properties.useMojangServices()) {
            try {
                XboxToken xstsMojangToken = acquireXstsToken(xblToken.token(), "rp://api.minecraftservices.com/");
                String minecraftToken = acquireMinecraftAccessToken(xstsMojangToken.userHash(), xstsMojangToken.token());
                return fetchMinecraftProfile(minecraftToken);
            } catch (MinecraftOAuthException ex) {
                String msg = ex.getMessage();
                if (msg != null && (msg.contains("Invalid app registration") || msg.contains("403"))) {
                    log.warn("Mojang Services rejected unapproved app registration. Falling back to Xbox Live Gamertag + Mojang API.");
                    return resolveProfileViaXboxLive(xblToken);
                }
                throw ex;
            }
        }

        return resolveProfileViaXboxLive(xblToken);
    }

    private MinecraftProfile resolveProfileViaXboxLive(XboxToken xblToken) {
        XboxToken xstsXbox = acquireXstsToken(xblToken.token(), "http://xboxlive.com");
        String gamertag = xstsXbox.gamertag();
        if (gamertag == null || gamertag.isBlank()) {
            gamertag = xblToken.gamertag();
        }
        if (gamertag == null || gamertag.isBlank()) {
            gamertag = fetchGamertagFromProfileApi(xstsXbox);
        }
        if (gamertag == null || gamertag.isBlank()) {
            throw new MinecraftOAuthException("Не удалось получить имя игрока (Gamertag) из аккаунта Xbox Live");
        }
        return resolveMojangProfileByGamertag(gamertag, xstsXbox.xuid());
    }

    private String fetchGamertagFromProfileApi(XboxToken token) {
        try {
            JsonNode response = restClient.get()
                    .uri("https://profile.xboxlive.com/users/me/profile/settings?settings=Gamertag")
                    .header(HttpHeaders.AUTHORIZATION, "XBL3.0 x=" + token.userHash() + ";" + token.token())
                    .header("x-xbl-contract-version", "2")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("profileUsers")) {
                JsonNode profileUsers = response.get("profileUsers");
                if (profileUsers.isArray() && !profileUsers.isEmpty()) {
                    JsonNode settings = profileUsers.get(0).path("settings");
                    if (settings.isArray()) {
                        for (JsonNode setting : settings) {
                            if ("Gamertag".equalsIgnoreCase(setting.path("name").asText())) {
                                return setting.path("value").asText();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to fetch Xbox profile settings: {}", ex.getMessage());
        }
        return null;
    }

    private MinecraftProfile resolveMojangProfileByGamertag(String gamertag, String xuid) {
        String safeName = gamertag.trim();
        try {
            MojangProfileResponse response = restClient.get()
                    .uri("https://api.mojang.com/users/profiles/minecraft/{username}", safeName)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(MojangProfileResponse.class);

            if (response != null && response.id() != null && response.name() != null) {
                log.info("Found official Mojang profile for {}: uuid={}, name={}", safeName, response.id(), response.name());
                return new MinecraftProfile(parseMojangUuid(response.id()), response.name());
            }
        } catch (RestClientResponseException ex) {
            log.info("No official Minecraft Java profile found for gamertag {} (status {}), using Xbox Live gamertag",
                    safeName, ex.getStatusCode());
        } catch (Exception ex) {
            log.warn("Failed to query Mojang public API for gamertag {}: {}", safeName, ex.getMessage());
        }

        UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + safeName).getBytes(StandardCharsets.UTF_8));
        log.info("Using deterministic offline UUID for Xbox user {}: {}", safeName, offlineUuid);
        return new MinecraftProfile(offlineUuid, safeName);
    }

    private String resolveRedirectUri(String customRedirectUri) {
        if (customRedirectUri != null && !customRedirectUri.isBlank()) {
            return customRedirectUri;
        }
        if (properties.redirectUri() != null && !properties.redirectUri().isBlank()) {
            return properties.redirectUri();
        }
        throw new MinecraftOAuthException("Microsoft OAuth redirect_uri is not configured");
    }

    private String acquireMicrosoftAccessToken(String code, String redirectUri) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", properties.clientId());
        formData.add("client_secret", properties.clientSecret());
        formData.add("code", code);
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", redirectUri);

        try {
            MsTokenResponse response = restClient.post()
                    .uri(MS_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(MsTokenResponse.class);

            if (response == null || response.accessToken() == null) {
                throw new MinecraftOAuthException("Empty response from Microsoft OAuth token endpoint");
            }
            return response.accessToken();
        } catch (RestClientResponseException ex) {
            log.error("Microsoft OAuth exchange failed with status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MinecraftOAuthException(extractErrorMessage(ex, "Ошибка авторизации Microsoft"), ex);
        } catch (Exception ex) {
            log.error("Unexpected error exchanging Microsoft OAuth code", ex);
            throw new MinecraftOAuthException("Не удалось связаться с Microsoft OAuth сервисом", ex);
        }
    }

    private XboxToken acquireXboxLiveToken(String msAccessToken) {
        Map<String, Object> body = Map.of(
                "Properties", Map.of(
                        "AuthMethod", "RPS",
                        "SiteName", "user.auth.xboxlive.com",
                        "RpsTicket", "d=" + msAccessToken
                ),
                "RelyingParty", "http://auth.xboxlive.com",
                "TokenType", "JWT"
        );

        try {
            XboxAuthResponse response = restClient.post()
                    .uri(XBL_AUTH_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(XboxAuthResponse.class);

            return extractXboxToken(response, "Xbox Live");
        } catch (RestClientResponseException ex) {
            log.error("Xbox Live authentication failed with status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MinecraftOAuthException(extractErrorMessage(ex, "Ошибка авторизации Xbox Live"), ex);
        } catch (Exception ex) {
            log.error("Unexpected error authenticating with Xbox Live", ex);
            throw new MinecraftOAuthException("Не удалось связаться с сервисом Xbox Live", ex);
        }
    }

    private XboxToken acquireXstsToken(String xblToken, String relyingParty) {
        Map<String, Object> body = Map.of(
                "Properties", Map.of(
                        "SandboxId", "RETAIL",
                        "UserTokens", List.of(xblToken)
                ),
                "RelyingParty", relyingParty,
                "TokenType", "JWT"
        );

        try {
            XboxAuthResponse response = restClient.post()
                    .uri(XSTS_AUTH_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(XboxAuthResponse.class);

            return extractXboxToken(response, "XSTS");
        } catch (RestClientResponseException ex) {
            log.error("XSTS authorization failed for {} with status {}: {}", relyingParty, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MinecraftOAuthException(extractErrorMessage(ex, "Ошибка авторизации XSTS"), ex);
        } catch (Exception ex) {
            log.error("Unexpected error authorizing with XSTS for " + relyingParty, ex);
            throw new MinecraftOAuthException("Не удалось связаться с сервисом XSTS", ex);
        }
    }

    private XboxToken extractXboxToken(XboxAuthResponse response, String serviceName) {
        if (response == null || response.token() == null) {
            throw new MinecraftOAuthException("Пустой ответ от " + serviceName);
        }
        if (response.displayClaims() == null || response.displayClaims().xui() == null || response.displayClaims().xui().isEmpty()) {
            throw new MinecraftOAuthException("Отсутствуют клеймы пользователя в ответе " + serviceName);
        }
        XuiClaim claim = response.displayClaims().xui().get(0);
        String uhs = claim.uhs();
        if (uhs == null || uhs.isBlank()) {
            throw new MinecraftOAuthException("Отсутствует идентификатор uhs в ответе " + serviceName);
        }
        return new XboxToken(response.token(), uhs, claim.gtg(), claim.xid());
    }

    private String acquireMinecraftAccessToken(String userHash, String xstsToken) {
        Map<String, String> body = Map.of(
                "identityToken", "XBL3.0 x=" + userHash + ";" + xstsToken
        );

        try {
            MinecraftLoginResponse response = restClient.post()
                    .uri(MINECRAFT_LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(MinecraftLoginResponse.class);

            if (response == null || response.accessToken() == null) {
                throw new MinecraftOAuthException("Пустой ответ от Minecraft Services при входе");
            }
            return response.accessToken();
        } catch (RestClientResponseException ex) {
            log.error("Minecraft login failed with status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MinecraftOAuthException(extractErrorMessage(ex, "Ошибка входа в Minecraft Services"), ex);
        } catch (Exception ex) {
            log.error("Unexpected error authenticating with Minecraft Services", ex);
            throw new MinecraftOAuthException("Не удалось связаться с Minecraft Services", ex);
        }
    }

    private MinecraftProfile fetchMinecraftProfile(String minecraftAccessToken) {
        try {
            MojangProfileResponse response = restClient.get()
                    .uri(MINECRAFT_PROFILE_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + minecraftAccessToken)
                    .header(HttpHeaders.USER_AGENT, USER_AGENT)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, resp) -> {
                        throw new MinecraftProfileNotFoundException();
                    })
                    .body(MojangProfileResponse.class);

            if (response == null || response.id() == null || response.name() == null) {
                throw new MinecraftProfileNotFoundException();
            }

            UUID uuid = parseMojangUuid(response.id());
            return new MinecraftProfile(uuid, response.name());
        } catch (MinecraftProfileNotFoundException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            log.error("Minecraft profile fetch failed with status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MinecraftOAuthException(extractErrorMessage(ex, "Ошибка получения профиля Minecraft"), ex);
        } catch (Exception ex) {
            log.error("Unexpected error fetching Minecraft profile", ex);
            throw new MinecraftOAuthException("Не удалось получить профиль Minecraft", ex);
        }
    }

    private String extractErrorMessage(RestClientResponseException ex, String defaultPrefix) {
        String responseBody = ex.getResponseBodyAsString();
        if (responseBody != null && !responseBody.isBlank()) {
            try {
                com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseBody);
                if (root.hasNonNull("error_description")) {
                    return root.get("error_description").asText();
                }
                if (root.hasNonNull("message")) {
                    return root.get("message").asText();
                }
                if (root.hasNonNull("errorMessage")) {
                    return root.get("errorMessage").asText();
                }
            } catch (Exception ignored) {
            }
        }
        return defaultPrefix + ": " + ex.getStatusCode().value();
    }

    public static UUID parseMojangUuid(String rawId) {
        if (rawId == null || rawId.length() != 32) {
            throw new IllegalArgumentException("Неверный формат Mojang UUID: " + rawId);
        }
        String formatted = rawId.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
                "$1-$2-$3-$4-$5"
        );
        return UUID.fromString(formatted);
    }

    private record XboxToken(String token, String userHash, String gamertag, String xuid) {
    }

    private record MsTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") Long expiresIn,
            @JsonProperty("refresh_token") String refreshToken
    ) {
    }

    private record XboxAuthResponse(
            @JsonProperty("Token") String token,
            @JsonProperty("DisplayClaims") DisplayClaims displayClaims
    ) {
    }

    private record DisplayClaims(
            @JsonProperty("xui") List<XuiClaim> xui
    ) {
    }

    private record XuiClaim(
            @JsonProperty("uhs") String uhs,
            @JsonProperty("gtg") String gtg,
            @JsonProperty("xid") String xid
    ) {
    }

    private record MinecraftLoginResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") Long expiresIn
    ) {
    }

    private record MojangProfileResponse(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name
    ) {
    }

    private record MsDeviceCodeResponse(
            @JsonProperty("device_code") String deviceCode,
            @JsonProperty("user_code") String userCode,
            @JsonProperty("verification_uri") String verificationUri,
            @JsonProperty("expires_in") Integer expiresIn,
            @JsonProperty("interval") Integer interval,
            @JsonProperty("message") String message
    ) {
    }
}
