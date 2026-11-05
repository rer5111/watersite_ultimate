package org.example.userservice.service;

import org.example.userservice.model.AccountModel;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountService {
    List<AccountModel> findById(long id);
    List<AccountModel> findByName(String name);
    Optional<AccountModel> findByUuid(UUID uuid);
    List<AccountModel> findAll();
    void addAccount(@NonNull AccountModel account);
    void updateAccount(@NonNull AccountModel account);
    void deleteAccount(long id);
    List<AccountModel> findPage(int page);
    int getPages();
}
