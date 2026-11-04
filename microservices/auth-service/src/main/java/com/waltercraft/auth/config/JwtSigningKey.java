package com.waltercraft.auth.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public record JwtSigningKey(RSAPrivateKey privateKey, RSAPublicKey publicKey, String keyId) {
}
