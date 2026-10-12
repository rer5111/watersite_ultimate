package org.example.p3.service;

import org.example.p3.model.UserModel;

import java.util.List;

public interface UserService {
    List<UserModel> findById(long id);
    List<UserModel> findByName(String name);
    List<UserModel> findAll();
    void addUser(UserModel User);
    void updateUser(UserModel User);
    void deleteUser(long id);
    List<UserModel> findPage(int page);
    int getPages();
}
