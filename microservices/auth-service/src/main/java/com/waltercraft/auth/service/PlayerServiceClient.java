package com.waltercraft.auth.service;

import com.waltercraft.auth.dto.AuthenticatedUser;

import java.util.UUID;

public interface PlayerServiceClient {
    AuthenticatedUser loginWithMinecraft(UUID uuid, String username);
    void linkMinecraft(long userId, UUID uuid, String username);
    void checkAvailability(String email, String username);
    AuthenticatedUser register(String email, String username, String password);
}
