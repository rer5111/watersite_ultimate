package com.waltercraft.auth.service;

import com.waltercraft.auth.dto.AuthenticatedUser;

public interface CredentialVerifier {
    AuthenticatedUser verify(String username, String password);
}
