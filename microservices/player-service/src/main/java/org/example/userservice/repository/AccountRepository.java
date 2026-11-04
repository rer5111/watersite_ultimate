package org.example.userservice.repository;

import org.example.userservice.model.AccountModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<AccountModel, Long> {
    List<AccountModel> findByUsername(String name);
    List<AccountModel> findAllById(long id);
}
