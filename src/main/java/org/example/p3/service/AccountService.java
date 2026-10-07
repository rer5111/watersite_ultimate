package org.example.p3.service;

import org.example.p3.model.AccountModel;
import org.example.p3.model.AccountModel;

import java.util.List;

public interface AccountService {
    public List<AccountModel> findByID(long id);
    public List<AccountModel> findByName(String name);
    public List<AccountModel> findAll();
    public AccountModel addAccount(AccountModel Account);
    public AccountModel updateAccount(AccountModel Account);
    public void deleteAccount(long id);
    public List<AccountModel> findPage(int page);
    public int getPages();
}
