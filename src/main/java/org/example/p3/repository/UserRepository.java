package org.example.p3.repository;

import org.apache.catalina.User;
import org.example.p3.model.AccountModel;
import org.example.p3.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
    List<UserModel> findAllById(int id);
    List<UserModel> findAllByName(String name);

    String account(AccountModel account);
}
