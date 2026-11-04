package org.example.userservice.service;

import org.example.userservice.model.UserModel;
import org.springframework.lang.NonNull;

import java.util.List;

public interface UserService {
    List<UserModel> findById(long id);
    List<UserModel> findByName(String name);
    List<UserModel> findAll();
    List<UserModel> findAdmins();
    void addUser(@NonNull UserModel user);
    void updateUser(@NonNull UserModel user);
    void deleteUser(long id);
    List<UserModel> findPage(int page);
    int getPages();
}
