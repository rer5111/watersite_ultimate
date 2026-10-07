package org.example.p3.service;

import org.example.p3.model.AccountModel;
import org.example.p3.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository AccountRepository;

    public AccountServiceImpl(AccountRepository AccountRepository) {
        this.AccountRepository = AccountRepository;
    }

    @Override
    public List<AccountModel> findByID(long id) {
        return AccountRepository.findById(id).stream().toList();
    }

    @Override
    public List<AccountModel> findByName(String name) {
        return AccountRepository.findByUsername(name);
    }

    @Override
    public List<AccountModel> findAll() {
        return AccountRepository.findAll();
    }

    @Override
    public AccountModel addAccount(AccountModel account) {
        return AccountRepository.save(account);
    }

    @Override
    public AccountModel updateAccount(AccountModel account) {
        return AccountRepository.save(account);
    }

    @Override
    public void deleteAccount(long id) {
        AccountRepository.deleteById(id);
    }

    @Override
    public List<AccountModel> findPage(int page){
        List<AccountModel> accounts = AccountRepository.findAll();
        if (accounts.isEmpty()){
            return new ArrayList<>();
        } else if (accounts.size() <page*5-5 || page < 1) {
            return new ArrayList<>();
        }
        if (page*5 < accounts.size()){
            return accounts.subList(page*5-5, page*5);
        }
        else{
            return accounts.subList(page*5-5, accounts.size());
        }
    }
    @Override
    public int getPages(){
        return (AccountRepository.findAll().size()-1)/5+1;
    }
}
