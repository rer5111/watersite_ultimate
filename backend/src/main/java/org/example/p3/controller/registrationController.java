package org.example.p3.controller;

import lombok.RequiredArgsConstructor;
import org.example.p3.model.UserModel;
import org.example.p3.model.AccountModel;
import org.example.p3.model.RoleEnum;
import org.example.p3.service.AccountService;
import org.example.p3.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class registrationController {

    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @GetMapping("/registration")
    private String RegisterView()
    {
        return "register";
    }

    @PostMapping("/registration")
    private String Register(String name, String accountName, String password, Model model)
    {
        List<UserModel> user_from_db = userService.findByName(name);
        List<AccountModel> acc_from_db = accountService.findByName(accountName);
        if (!user_from_db.isEmpty())
        {
            model.addAttribute("message", "Пользователь с таким логином уже существует");
            return "register";
        }
        if (!acc_from_db.isEmpty())
        {
            model.addAttribute("message", "Такой аккаунт уже существует");
            return "register";
        }
        AccountModel acc = new AccountModel(0, accountName, LocalDate.now());
        accountService.addAccount(acc);
        UserModel user = new UserModel(0, name, passwordEncoder.encode(password), Set.of(RoleEnum.USER, RoleEnum.ADMIN), accountService.findById(acc.getId()).get(0));
        userService.addUser(user);
        return "redirect:/login";
    }
}
