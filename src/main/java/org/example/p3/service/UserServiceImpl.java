package org.example.p3.service;

import org.example.p3.model.SeasonModel;
import org.example.p3.model.UserModel;
import org.example.p3.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository UserRepository;

    public UserServiceImpl(UserRepository UserRepository) {
        this.UserRepository = UserRepository;
    }

    @Override
    public List<UserModel> findByID(long id) {
        return UserRepository.findById(id).stream().toList();
    }

    @Override
    public List<UserModel> findByName(String name) {
        return UserRepository.findAllByName(name);
    }

    @Override
    public List<UserModel> findAll() {
        return UserRepository.findAll();
    }

    @Override
    public UserModel addUser(UserModel User) {
        return UserRepository.save(User);
    }

    @Override
    public UserModel updateUser(UserModel User) {
        return UserRepository.save(User);
    }

    @Override
    public void deleteUser(long id) {
        UserRepository.deleteById(id);
    }
    
    @Override
    public List<UserModel> findPage(int page){
        List<UserModel> Users = UserRepository.findAll();
        if (Users.isEmpty()){
            return new ArrayList<>();
        } else if (Users.size() <page*5-5 || page < 1) {
            return new ArrayList<>();
        }
        if (page*5 < Users.size()){
            return Users.subList(page*5-5, page*5);
        }
        else{
            return Users.subList(page*5-5, Users.size());
        }
    }
    @Override
    public int getPages(){
        return (UserRepository.findAll().size()-1)/5+1;
    }
}
