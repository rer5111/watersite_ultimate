package org.example.p3.controller;

import org.example.p3.model.UserModel;
import org.example.p3.model.RoleEnum;
import org.example.p3.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

import java.time.LocalDate;

@Controller
public class MainController {
    @GetMapping("/")
    public String Index(Model model){
        return "index";
    }
}
