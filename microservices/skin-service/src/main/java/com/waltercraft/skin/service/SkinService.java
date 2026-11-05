package com.waltercraft.skin.service;

import com.waltercraft.skin.config.SkinProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SkinService {

    private static final Logger log = LoggerFactory.getLogger(SkinService.class);
    private static final int MAX_CACHE_ENTRIES = 1000;
    private static final String DEFAULT_SKIN_SUBJECT = "X-Steve";

    private record CachedSkin(byte[] data, Instant expiresAt) {}

    private final HttpClient httpClient;
    private final SkinProperties properties;
    private final Map<String, CachedSkin> imageCache = Collections.synchronizedMap(
            new LinkedHashMap<String, CachedSkin>(128, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, CachedSkin> eldest) {
                    return size() > MAX_CACHE_ENTRIES;
                }
            }
    );

    public SkinService(SkinProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.connectTimeoutMs()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public Optional<byte[]> getFullByUuid(UUID uuid) {
        return getFullBySubject(uuid.toString())
                .or(() -> getFullBySubject(DEFAULT_SKIN_SUBJECT));
    }

    public Optional<byte[]> getFullBySubject(String subject) {
        Optional<byte[]> skin = getOrFetch("full:" + subject.toLowerCase(), new String[]{
                properties.vzgeUrl() + "/full/384/" + subject + ".png",
                properties.crafatarUrl() + "/renders/body/" + subject + "?overlay=true",
                properties.visageUrl() + "/full/384/" + subject
        });
        if (skin.isPresent() || DEFAULT_SKIN_SUBJECT.equalsIgnoreCase(subject)) {
            return skin;
        }
        return getFullBySubject(DEFAULT_SKIN_SUBJECT);
    }

    public Optional<byte[]> getBustByUuid(UUID uuid) {
        return getBustBySubject(uuid.toString())
                .or(() -> getBustBySubject(DEFAULT_SKIN_SUBJECT));
    }

    public Optional<byte[]> getBustBySubject(String subject) {
        Optional<byte[]> skin = getOrFetch("bust:" + subject.toLowerCase(), new String[]{
                properties.vzgeUrl() + "/bust/384/" + subject + ".png",
                properties.visageUrl() + "/bust/384/" + subject,
                properties.crafatarUrl() + "/renders/body/" + subject + "?overlay=true"
        });
        if (skin.isPresent() || DEFAULT_SKIN_SUBJECT.equalsIgnoreCase(subject)) {
            return skin;
        }
        return getBustBySubject(DEFAULT_SKIN_SUBJECT);
    }

    public Optional<byte[]> getFaceByUuid(UUID uuid) {
        return getFaceBySubject(uuid.toString())
                .or(() -> getFaceBySubject(DEFAULT_SKIN_SUBJECT));
    }

    public Optional<byte[]> getFaceBySubject(String subject) {
        Optional<byte[]> skin = getOrFetch("face:" + subject.toLowerCase(), new String[]{
                properties.vzgeUrl() + "/face/256/" + subject + ".png",
                properties.crafatarUrl() + "/avatars/" + subject + "?overlay=true&size=128",
                properties.visageUrl() + "/face/128/" + subject
        });
        if (skin.isPresent() || DEFAULT_SKIN_SUBJECT.equalsIgnoreCase(subject)) {
            return skin;
        }
        return getFaceBySubject(DEFAULT_SKIN_SUBJECT);
    }

    public Optional<byte[]> getFaceByUsername(String username) {
        return getOrFetch("user:" + username.toLowerCase(), new String[]{
                properties.vzgeUrl() + "/face/256/" + username + ".png",
                "https://minotar.net/avatar/" + username + "/128",
                properties.crafatarUrl() + "/avatars/" + username + "?overlay=true"
        }).or(() -> getFaceBySubject(DEFAULT_SKIN_SUBJECT));
    }

    private Optional<byte[]> getOrFetch(String cacheKey, String[] urls) {
        CachedSkin cached = imageCache.get(cacheKey);
        if (cached != null) {
            if (cached.expiresAt().isAfter(Instant.now())) {
                return Optional.of(cached.data());
            } else {
                imageCache.remove(cacheKey);
            }
        }

        for (String url : urls) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofMillis(properties.readTimeoutMs()))
                        .header("User-Agent", properties.userAgent())
                        .header("Accept", "image/png, image/*")
                        .GET()
                        .build();

                HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() == 200 && response.body() != null && response.body().length > 0) {
                    Instant expiresAt = Instant.now().plus(Duration.ofMinutes(properties.cacheTtlMinutes()));
                    imageCache.put(cacheKey, new CachedSkin(response.body(), expiresAt));
                    return Optional.of(response.body());
                } else {
                    log.debug("HTTP {} from {}", response.statusCode(), url);
                }
            } catch (Exception e) {
                log.debug("Failed to fetch skin from {}: {}", url, e.getMessage());
            }
        }
        return Optional.empty();
    }
}
