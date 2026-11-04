package org.example.userservice.repository;

import org.example.userservice.model.AccountModel;
import org.example.userservice.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
    List<UserModel> findAllById(long id);
    List<UserModel> findAllByName(String name);
    List<UserModel> findByAccount(AccountModel account);
}
