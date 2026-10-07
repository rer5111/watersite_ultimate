package org.example.p3.controller;

import org.example.p3.model.UserModel;
import org.example.p3.model.AccountModel;
import org.example.p3.model.RoleEnum;
import org.example.p3.service.AccountService;
import org.example.p3.repository.UserRepository;
import org.example.p3.repository.AccountRepository;
import org.example.p3.service.AccountService;
import org.example.p3.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import java.util.Collections;

@Controller
public class registrationController {
    @Autowired
    private org.example.p3.service.UserService UserService;
    @Autowired
    private org.example.p3.service.AccountService AccountService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;

    @GetMapping("/registration")
    private String RegView()
    {
        return "regis";
    }
    @PostMapping("/registration")
    private String Reg(String name, String account_name, String password, Model model)
    {
        List<UserModel> user_from_db = UserService.findByName(name);
        List<AccountModel> acc_from_db = AccountService.findByName(account_name);
        if (!user_from_db.isEmpty())
        {
            model.addAttribute("message", "Пользователь с таким логином уже существует");
            return "regis";
        }
        if (!acc_from_db.isEmpty())
        {
            model.addAttribute("message", "Такой аккаунт уже существует");
            return "regis";
        }
        AccountModel acc = new AccountModel(0, account_name, LocalDate.now());
        AccountService.addAccount(acc);
        UserModel user = new UserModel(0, name, passwordEncoder.encode(password), Set.of(RoleEnum.USER, RoleEnum.ADMIN), AccountService.findByID(acc.getId()).get(0));
        userService.addUser(user);
        return "redirect:/login";
    }
}
