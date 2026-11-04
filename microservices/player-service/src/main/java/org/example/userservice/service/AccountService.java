package org.example.userservice.service;

import org.example.userservice.model.AccountModel;
import org.springframework.lang.NonNull;

import java.util.List;

public interface AccountService {
    List<AccountModel> findById(long id);
    List<AccountModel> findByName(String name);
    List<AccountModel> findAll();
    void addAccount(@NonNull AccountModel account);
    void updateAccount(@NonNull AccountModel account);
    void deleteAccount(long id);
    List<AccountModel> findPage(int page);
    int getPages();
}
