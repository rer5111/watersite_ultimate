package org.example.userservice.controller;

import org.example.userservice.model.AccountModel;
import org.example.userservice.model.RoleEnum;
import org.example.userservice.model.SeasonModel;
import org.example.userservice.model.UserModel;
import org.example.userservice.dto.AdminProfileResponse;
import org.example.userservice.service.AccountService;
import org.example.userservice.service.SeasonService;
import org.example.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final SeasonService seasonService;

    public UserController(
            UserService userService,
            AccountService accountService,
            PasswordEncoder passwordEncoder,
            SeasonService seasonService
    ) {
        this.userService = userService;
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        this.seasonService = seasonService;
    }

    @PutMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus registerUser(
            @RequestParam String name,
            @RequestParam String password,
            @RequestParam Set<RoleEnum> roles,
            @RequestParam int accountId
    ) {
        try {
            List<AccountModel> accounts = accountService.findById(accountId);
            if (accounts.isEmpty()) {
                return HttpStatus.BAD_REQUEST;
            }
            UserModel userModel = new UserModel(
                    0,
                    name,
                    passwordEncoder.encode(password),
                    roles,
                    accounts.get(0)
            );
            userService.addUser(userModel);
            return HttpStatus.OK;
        } catch (Exception e) {
            log.error("Failed to register user: {}", e.getMessage());
            return HttpStatus.BAD_REQUEST;
        }
    }

    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus updateUser(
            @RequestParam int id,
            @RequestParam String name,
            @RequestParam String password,
            @RequestParam Set<RoleEnum> roles,
            @RequestParam int accountId
    ) {
        List<UserModel> users = userService.findById(id);
        List<AccountModel> accounts = accountService.findById(accountId);
        if (users.isEmpty() || accounts.isEmpty()) {
            return HttpStatus.NOT_FOUND;
        }

        try {
            UserModel userModel = users.get(0);
            userModel.setName(name);
            userModel.setPassword(passwordEncoder.encode(password));
            userModel.setRoles(roles);
            userModel.setAccount(accounts.get(0));
            userService.updateUser(userModel);
            return HttpStatus.OK;
        } catch (Exception e) {
            log.error("Failed to update user: {}", e.getMessage());
            return HttpStatus.BAD_REQUEST;
        }
    }

    @DeleteMapping(value = "/{key}", produces = APPLICATION_JSON_VALUE)
    public HttpStatus deleteUser(@PathVariable int key) {
        try {
            List<UserModel> users = userService.findById(key);
            if (users.isEmpty()) {
                return HttpStatus.NOT_FOUND;
            }
            accountService.deleteAccount(users.get(0).getAccount().getId());
            return HttpStatus.OK;
        } catch (Exception e) {
            log.error("Failed to delete user: {}", e.getMessage());
            return HttpStatus.NOT_FOUND;
        }
    }

    @GetMapping(path = "/pages", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> getPages() {
        return ResponseEntity.ok(userService.getPages());
    }

    @GetMapping(path = "/admins", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AdminProfileResponse>> getAdmins() {
        List<AdminProfileResponse> admins = userService.findAdmins().stream()
                .map(user -> new AdminProfileResponse(
                        user.getAccount() != null ? user.getAccount().getUsername() : user.getName(),
                        user.getAccount() != null ? user.getAccount().getUuid() : null
                ))
                .toList();
        return ResponseEntity.ok(admins);
    }

    @GetMapping(path = "/all", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserModel>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping(path = "/findPage/{page}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserModel>> getPage(@PathVariable int page) {
        return ResponseEntity.ok(userService.findPage(page));
    }

    @GetMapping(value = "/findID/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserModel>> findById(@PathVariable int id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping(value = "/findName/{name}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserModel>> findByName(@PathVariable String name) {
        return ResponseEntity.ok(userService.findByName(name));
    }

    @GetMapping(value = "/deleteSeason/{id}/{s_id}", produces = APPLICATION_JSON_VALUE)
    public HttpStatus deleteFromSeason(@PathVariable int id, @PathVariable int s_id) {
        log.info("Deleting user {} from season {}", id, s_id);
        try {
            List<UserModel> users = userService.findById(id);
            List<SeasonModel> seasons = seasonService.findById(s_id);
            if (users.isEmpty() || seasons.isEmpty()) {
                return HttpStatus.NOT_FOUND;
            }
            UserModel userModel = users.get(0);
            userModel.removeSeason(seasons.get(0));
            userService.updateUser(userModel);
            return HttpStatus.OK;
        } catch (Exception e) {
            log.error("Failed to delete user from season: {}", e.getMessage());
            return HttpStatus.BAD_REQUEST;
        }
    }

    @GetMapping(value = "/addSeason/{id}/{s_id}", produces = APPLICATION_JSON_VALUE)
    public HttpStatus addToSeason(@PathVariable int id, @PathVariable int s_id) {
        log.info("Adding user {} to season {}", id, s_id);
        try {
            List<UserModel> users = userService.findById(id);
            List<SeasonModel> seasons = seasonService.findById(s_id);
            if (users.isEmpty() || seasons.isEmpty()) {
                return HttpStatus.NOT_FOUND;
            }
            UserModel userModel = users.get(0);
            userModel.addSeason(seasons.get(0));
            userService.updateUser(userModel);
            log.info("User {} added to season {}", id, s_id);
            return HttpStatus.OK;
        } catch (Exception e) {
            log.error("Failed to add user to season: {}", e.getMessage());
            return HttpStatus.BAD_REQUEST;
        }
    }
}
