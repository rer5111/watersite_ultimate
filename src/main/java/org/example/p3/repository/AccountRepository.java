package org.example.p3.repository;

import org.example.p3.model.AccountModel;
import org.example.p3.model.AccountModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<AccountModel, Long> {
    List<AccountModel> findByUsername(String name);
    List<AccountModel> findById(int id);
}
