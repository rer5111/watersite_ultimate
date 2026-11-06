package com.waltercraft.skin.service;

import com.waltercraft.skin.config.SkinProperties;
import com.waltercraft.skin.exception.SkinProviderUnavailableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
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
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final Map<String, CachedSkin> imageCache = Collections.synchronizedMap(
            new LinkedHashMap<String, CachedSkin>(128, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, CachedSkin> eldest) {
                    return size() > MAX_CACHE_ENTRIES;
                }
            }
    );

    public SkinService(SkinProperties properties, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.properties = properties;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
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
        boolean providerUnavailable = false;
        if (cached != null) {
            if (cached.expiresAt().isAfter(Instant.now())) {
                return Optional.of(cached.data());
            }
        }

        for (String url : urls) {
            String providerHost = URI.create(url).getHost();
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("skin-" + providerHost);
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofMillis(properties.readTimeoutMs()))
                        .header("User-Agent", properties.userAgent())
                        .header("Accept", "image/png, image/*")
                        .GET()
                        .build();

                HttpResponse<byte[]> response = circuitBreaker.executeSupplier(() -> send(request));
                if (response.statusCode() == 200 && response.body() != null && response.body().length > 0) {
                    Instant expiresAt = Instant.now().plus(Duration.ofMinutes(properties.cacheTtlMinutes()));
                    imageCache.put(cacheKey, new CachedSkin(response.body(), expiresAt));
                    return Optional.of(response.body());
                } else {
                    log.debug("Skin provider returned no image providerHost={} status={}", providerHost, response.statusCode());
                }
            } catch (CallNotPermittedException exception) {
                providerUnavailable = true;
                log.warn("Skin provider circuit is open providerHost={} cachedFallbackAvailable={}",
                        providerHost, cached != null);
            } catch (SkinProviderUnavailableException exception) {
                providerUnavailable = true;
                log.warn("Skin provider fallback activated providerHost={} exceptionType={}",
                        providerHost, exception.getClass().getSimpleName());
            } catch (Exception e) {
                log.debug("Skin provider request failed providerHost={} exceptionType={}", providerHost,
                        e.getClass().getSimpleName());
            }
        }
        if (cached != null) {
            long staleAgeSeconds = Math.max(0, Duration.between(cached.expiresAt(), Instant.now()).getSeconds());
            log.warn("Using stale skin cache after provider failures staleAgeSeconds={}", staleAgeSeconds);
            return Optional.of(cached.data());
        }
        if (providerUnavailable) {
            throw new SkinProviderUnavailableException("All skin providers are temporarily unavailable");
        }
        return Optional.empty();
    }

    private HttpResponse<byte[]> send(HttpRequest request) {
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() >= 500 || response.statusCode() == 429) {
                throw new SkinProviderUnavailableException("Skin provider returned HTTP " + response.statusCode());
            }
            return response;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new SkinProviderUnavailableException(exception);
        } catch (IOException exception) {
            throw new SkinProviderUnavailableException(exception);
        }
    }
}
