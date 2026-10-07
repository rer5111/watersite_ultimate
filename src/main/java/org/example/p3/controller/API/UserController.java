package org.example.p3.controller.API;

import org.example.p3.model.RoleEnum;
import org.example.p3.model.SeasonModel;
import org.example.p3.model.UserModel;
import org.example.p3.service.AccountService;
import org.example.p3.service.SeasonService;
import org.example.p3.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SeasonService seasonService;

    public UserController(UserService service) {
        this.userService = service;
    }

    @Tag(name = "Добавление", description = "Добавляет запись")
    @PutMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus registerUser(@RequestParam String name, @RequestParam String password, @RequestParam Set<RoleEnum> roles, @RequestParam int accountId) {
        try {
            UserModel userModel = new UserModel(0, name, passwordEncoder.encode(password), roles ,accountService.findByID(accountId).get(0));
            userService.addUser(userModel);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    @Tag(name = "Изменение", description = "Изменяет запись")
    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus updateUser(@RequestParam int id, @RequestParam String name, @RequestParam String password, @RequestParam Set<RoleEnum> roles, @RequestParam int accountId) {
        if (userService.findByID(id).isEmpty() || accountService.findByID(accountId).isEmpty()) return HttpStatus.NOT_FOUND;
        try {
            UserModel userModel = userService.findByID(id).get(0);
            userModel.setName(name);
            userModel.setPassword(passwordEncoder.encode(password));
            userModel.setRoles(roles);
            userModel.setAccount_ID(accountService.findByID(accountId).get(0));
            userService.updateUser(userModel);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    @Tag(name = "Удаление", description = "Удаляет запись")
    @DeleteMapping(value = "/{key}", produces = APPLICATION_JSON_VALUE)
    public HttpStatus deleteDonation(@PathVariable int key) {
        try {
            accountService.deleteAccount(userService.findByID(key).get(0).getAccount_ID().getId());
        }catch (Exception e){
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.OK;
    }

    @Tag(name = "Нахождение всех страниц", description = "Находит страницы")
    @GetMapping(path = "/pages", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> getPages() {
        return ResponseEntity.ok(userService.getPages());
    }

    @Tag(name = "Нахождение всех", description = "Находит все записи")
    @GetMapping(path="/all", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserModel>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @Tag(name = "Нахождение страницы", description = "Находит страницу")
    @GetMapping(path="/findPage/{page}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserModel>> getPage(@PathVariable int page) {
        return ResponseEntity.ok(userService.findPage(page));
    }

    @Tag(name = "Нахождение по ID", description = "Находит запись по ID")
    @GetMapping(value="/findID/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserModel>> findById(@PathVariable int id) {
        return ResponseEntity.ok(userService.findByID(id));
    }

    @Tag(name = "Нахождение по имени", description = "Находит запись по имени")
    @GetMapping(value="/findName/{name}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserModel>> findByName(@PathVariable String name) {
        return ResponseEntity.ok(userService.findByName(name));
    }

    @Tag(name = "Удаление пользователя из сезона", description = "Удаляет из сезона")
    @GetMapping(value="/deleteSeason/{id}/{s_id}", produces = APPLICATION_JSON_VALUE)
    public HttpStatus deleteFromSeason(@PathVariable int id, @PathVariable int s_id) {
        System.out.println("we deleting");
        try{
            if (userService.findByID(id).isEmpty() || seasonService.findByID(s_id).isEmpty()) return HttpStatus.NOT_FOUND;
            UserModel userModel = userService.findByID(id).get(0);
            userModel.removeSeason(seasonService.findByID(s_id).get(0));
            userService.updateUser(userModel);
        return HttpStatus.OK;
        }
        catch (Exception e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    @Tag(name = "Добавление пользователя в сезон", description = "Добавляет в сезон")
    @GetMapping(value="/addSeason/{id}/{s_id}", produces = APPLICATION_JSON_VALUE)
    public HttpStatus addToSeason(@PathVariable int id, @PathVariable int s_id) {
        System.out.println("we adding");
        try{
            if (userService.findByID(id).isEmpty() || seasonService.findByID(s_id).isEmpty()) return HttpStatus.NOT_FOUND;
            UserModel userModel = userService.findByID(id).get(0);
            userModel.addSeason(seasonService.findByID(s_id).get(0));
            userService.updateUser(userModel);
            System.out.println("we succeeded");
            return HttpStatus.OK;
        }
        catch (Exception e) {
            System.out.println("we failed");
            return HttpStatus.BAD_REQUEST;
        }
    }

}
