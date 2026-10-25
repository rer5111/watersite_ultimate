package org.example.p3.service;

import org.example.p3.model.AccountModel;

import java.util.List;

public interface AccountService {
    List<AccountModel> findById(long id);
    List<AccountModel> findByName(String name);
    List<AccountModel> findAll();
    void addAccount(AccountModel Account);
    void updateAccount(AccountModel Account);
    void deleteAccount(long id);
    List<AccountModel> findPage(int page);
    int getPages();
}
