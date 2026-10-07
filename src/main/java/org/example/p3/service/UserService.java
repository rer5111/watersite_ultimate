package org.example.p3.service;

import org.example.p3.model.SeasonModel;
import org.example.p3.model.UserModel;

import java.util.List;

public interface UserService {
    public List<UserModel> findByID(long id);
    public List<UserModel> findByName(String name);
    public List<UserModel> findAll();
    public UserModel addUser(UserModel User);
    public UserModel updateUser(UserModel User);
    public void deleteUser(long id);
    public List<UserModel> findPage(int page);
    public int getPages();
}
