package com.waltercraft.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Clock;
import java.util.Base64;
import java.util.UUID;

@Configuration
public class JwtConfiguration {

    @Bean
    public JwtSigningKey jwtSigningKey(JwtProperties properties) {
        validateProperties(properties);
        try {
            RSAPrivateKey privateKey = loadPrivateKey(properties);
            if (!(privateKey instanceof RSAPrivateCrtKey rsaPrivateKey)) {
                throw new IllegalStateException("JWT RSA private key must include CRT public key parameters");
            }
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(
                    new RSAPublicKeySpec(rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent())
            );
            String keyId = UUID.nameUUIDFromBytes(publicKey.getEncoded()).toString();
            return new JwtSigningKey(privateKey, publicKey, keyId);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to initialize the JWT signing key", exception);
        }
    }

    @Bean
    public RSAKey jwk(JwtSigningKey signingKey) {
        return new RSAKey.Builder(signingKey.publicKey())
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
                .keyID(signingKey.keyId())
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder(JwtSigningKey signingKey) {
        RSAKey signingJwk = new RSAKey.Builder(signingKey.publicKey())
                .privateKey(signingKey.privateKey())
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
                .keyID(signingKey.keyId())
                .build();
        JWKSource<SecurityContext> jwkSource = (selector, context) -> selector.select(new JWKSet(signingJwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtSigningKey signingKey, JwtProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(signingKey.publicKey()).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
        return decoder;
    }

    @Bean
    public JWKSet publicJwkSet(RSAKey jwk) {
        return new JWKSet(jwk.toPublicJWK());
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    private static RSAPrivateKey loadPrivateKey(JwtProperties properties) throws Exception {
        String encodedKey = properties.privateKeyBase64();
        if (encodedKey == null || encodedKey.isBlank()) {
            if (!properties.allowEphemeralKey()) {
                throw new IllegalStateException("AUTH_JWT_PRIVATE_KEY_BASE64 must be configured");
            }
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(3072);
            return (RSAPrivateKey) generator.generateKeyPair().getPrivate();
        }
        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    private static void validateProperties(JwtProperties properties) {
        if (properties.issuer() == null || properties.issuer().isBlank()) {
            throw new IllegalStateException("AUTH_JWT_ISSUER must be configured");
        }
        if (properties.accessTokenTtl() == null || properties.accessTokenTtl().isZero()
                || properties.accessTokenTtl().isNegative()) {
            throw new IllegalStateException("AUTH_ACCESS_TOKEN_TTL must be a positive duration");
        }
        if (properties.refreshTokenTtl() == null || properties.refreshTokenTtl().compareTo(properties.accessTokenTtl()) <= 0) {
            throw new IllegalStateException("AUTH_REFRESH_TOKEN_TTL must be longer than the access token lifetime");
        }
    }
}
