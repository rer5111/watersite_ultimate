package org.example.userservice.service;

import org.example.userservice.model.UserModel;
import org.example.userservice.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserModel> findById(long id) {
        return userRepository.findById(id).stream().toList();
    }

    @Override
    public List<UserModel> findByName(String name) {
        return userRepository.findAllByName(name);
    }

    @Override
    public List<UserModel> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void addUser(@NonNull UserModel user) {
        userRepository.save(Objects.requireNonNull(user));
    }

    @Override
    public void updateUser(@NonNull UserModel user) {
        userRepository.save(Objects.requireNonNull(user));
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<UserModel> findPage(int page) {
        if (page < 1) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(page - 1, 5);
        return userRepository.findAll(pageable).getContent();
    }

    @Override
    public int getPages() {
        Pageable pageable = PageRequest.of(0, 5);
        return userRepository.findAll(pageable).getTotalPages();
    }
}
