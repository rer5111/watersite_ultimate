package org.example.p3.service;

import lombok.RequiredArgsConstructor;
import org.example.p3.model.AccountModel;
import org.example.p3.repository.AccountRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public List<AccountModel> findById(long id) {
        return accountRepository.findById(id).stream().toList();
    }

    @Override
    public List<AccountModel> findByName(String name) {
        return accountRepository.findByUsername(name);
    }

    @Override
    public List<AccountModel> findAll() {
        return accountRepository.findAll();
    }

    @Override
    public void addAccount(AccountModel account) {
        accountRepository.save(account);
    }

    @Override
    public void updateAccount(AccountModel account) {
        accountRepository.save(account);
    }

    @Override
    public void deleteAccount(long id) {
        accountRepository.deleteById(id);
    }

    @Override
    public List<AccountModel> findPage(int page) {
        if (page < 1) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(page - 1, 5);
        return accountRepository.findAll(pageable).getContent();
    }

    @Override
    public int getPages() {
        Pageable pageable = PageRequest.of(0, 5);
        return accountRepository.findAll(pageable).getTotalPages();
    }
}
