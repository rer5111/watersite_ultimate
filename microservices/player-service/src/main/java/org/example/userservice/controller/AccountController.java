package org.example.userservice.controller;

import org.example.userservice.model.AccountModel;
import org.example.userservice.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PutMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus registerAccount(
            @RequestParam String name,
            @RequestParam LocalDate joinDate,
            @RequestParam(required = false) UUID uuid
    ) {
        try {
            AccountModel accountModel = new AccountModel(0, name, joinDate);
            accountModel.setUuid(uuid);
            service.addAccount(accountModel);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus updateAccount(
            @RequestParam int id,
            @RequestParam String name,
            @RequestParam LocalDate joinDate,
            @RequestParam(required = false) UUID uuid
    ) {
        List<AccountModel> accounts = service.findById(id);
        if (accounts.isEmpty()) {
            return HttpStatus.NOT_FOUND;
        }
        AccountModel accountModel = accounts.get(0);
        accountModel.setUsername(name);
        accountModel.setJoinDate(joinDate);
        if (uuid != null) {
            accountModel.setUuid(uuid);
        }
        service.updateAccount(accountModel);
        return HttpStatus.OK;
    }

    @DeleteMapping(value = "/{key}", produces = APPLICATION_JSON_VALUE)
    public HttpStatus deleteDonation(@PathVariable int key) {
        try {
            service.deleteAccount(key);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.NOT_FOUND;
        }
    }

    @GetMapping(path = "/pages", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> getPages() {
        return ResponseEntity.ok(service.getPages());
    }

    @GetMapping(path = "/all", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountModel>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping(path = "/findPage/{page}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountModel>> getPage(@PathVariable int page) {
        return ResponseEntity.ok(service.findPage(page));
    }

    @GetMapping(value = "/findID/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountModel>> findById(@PathVariable int id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping(value = "/findName/{name}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountModel>> findByName(@PathVariable String name) {
        return ResponseEntity.ok(service.findByName(name));
    }
}
