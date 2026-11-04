package com.waltercraft.auth.service;

import com.waltercraft.auth.config.JwtProperties;
import com.waltercraft.auth.config.JwtSigningKey;
import com.waltercraft.auth.dto.AuthenticatedUser;
import com.waltercraft.auth.dto.TokenResponse;
import com.waltercraft.auth.exception.InvalidRefreshTokenException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties properties;
    private final JwtSigningKey signingKey;
    private final Clock clock;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            JwtProperties properties,
            JwtSigningKey signingKey,
            Clock clock
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.properties = properties;
        this.signingKey = signingKey;
        this.clock = clock;
    }

    public TokenResponse issueTokens(AuthenticatedUser user) {
        Instant issuedAt = clock.instant();
        String accessToken = encode(user.username(), user.roles(), "access", issuedAt, properties.accessTokenTtl());
        String refreshToken = encode(user.username(), user.roles(), "refresh", issuedAt, properties.refreshTokenTtl());
        return new TokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                properties.accessTokenTtl().toSeconds()
        );
    }

    public TokenResponse refresh(String refreshToken) {
        try {
            Jwt jwt = jwtDecoder.decode(refreshToken);
            if (!"refresh".equals(jwt.getClaimAsString("token_type")) || jwt.getSubject() == null) {
                throw new InvalidRefreshTokenException();
            }
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null) {
                throw new InvalidRefreshTokenException();
            }
            return issueTokens(new AuthenticatedUser(null, jwt.getSubject(), java.util.Set.copyOf(roles)));
        } catch (JwtException | IllegalArgumentException exception) {
            throw new InvalidRefreshTokenException();
        }
    }

    public String keyId() {
        return signingKey.keyId();
    }

    private String encode(String username, Iterable<String> roles, String tokenType, Instant issuedAt, java.time.Duration ttl) {
        List<String> roleList = java.util.stream.StreamSupport.stream(roles.spliterator(), false)
                .sorted()
                .toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(username)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(ttl))
                .id(UUID.randomUUID().toString())
                .claim("token_type", tokenType)
                .claim("roles", roleList)
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(signingKey.keyId())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
