package org.example.p3.controller.API;

import org.example.p3.model.AccountModel;
import org.example.p3.service.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @Tag(name = "Добавление", description = "Добавляет запись")
    @PutMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus registerAccount(@RequestParam String name, @RequestParam LocalDate date) {
        try {
            AccountModel accountModel = new AccountModel(0, name, date);
            service.addAccount(accountModel);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    @Tag(name = "Изменение", description = "Изменяет запись")
    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus updateAccount(@RequestParam int id, @RequestParam String name, @RequestParam LocalDate date) {
        if (service.findByID(id).isEmpty()) return HttpStatus.NOT_FOUND;
        AccountModel accountModel = service.findByID(id).get(0);
        accountModel.setUsername(name);
        accountModel.setJoin_date(date);
        service.updateAccount(accountModel);
        return HttpStatus.OK;
    }

    @Tag(name = "Удаление", description = "Удаляет запись")
    @DeleteMapping(value = "/{key}", produces = APPLICATION_JSON_VALUE)
    public HttpStatus deleteDonation(@PathVariable int key) {
        try {
            service.deleteAccount(key);
        }catch (Exception e){
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.OK;
    }

    @Tag(name = "Нахождение всех страниц", description = "Находит страницы")
    @GetMapping(path = "/pages", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> getPages() {
        return ResponseEntity.ok(service.getPages());
    }

    @Tag(name = "Нахождение всех", description = "Находит все записи")
    @GetMapping(path="/all", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountModel>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Tag(name = "Нахождение страницы", description = "Находит страницу")
    @GetMapping(path="/findPage/{page}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountModel>> getPage(@PathVariable int page) {
        return ResponseEntity.ok(service.findPage(page));
    }

    @Tag(name = "Нахождение по ID", description = "Находит запись по ID")
    @GetMapping(value="/findID/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountModel>> findById(@PathVariable int id) {
        return ResponseEntity.ok(service.findByID(id));
    }

    @Tag(name = "Нахождение по имени", description = "Находит запись по имени")
    @GetMapping(value="/findName/{name}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountModel>> findByName(@PathVariable String name) {
        return ResponseEntity.ok(service.findByName(name));
    }

}
