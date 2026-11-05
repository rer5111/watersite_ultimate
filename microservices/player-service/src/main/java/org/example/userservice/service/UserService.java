package org.example.userservice.service;

import org.example.userservice.model.UserModel;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserModel> findById(long id);
    List<UserModel> findByName(String name);
    List<UserModel> findByIdentifier(String identifier);
    List<UserModel> findAll();
    List<UserModel> findAdmins();
    void addUser(@NonNull UserModel user);
    void updateUser(@NonNull UserModel user);
    void deleteUser(long id);
    List<UserModel> findPage(int page);
    int getPages();
    UserModel loginOrRegisterWithMinecraft(@NonNull UUID uuid, @NonNull String username);
    void linkMinecraft(long userId, @NonNull UUID uuid, @NonNull String username);
    UserModel registerUser(String email, String username, String password);
    org.example.userservice.dto.CheckAvailabilityResponse checkAvailability(String email, String username);
}
