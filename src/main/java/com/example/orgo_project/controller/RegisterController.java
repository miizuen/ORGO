package com.example.orgo_project.controller;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.Role;
import com.example.orgo_project.entity.UserProfile;
import com.example.orgo_project.enums.RoleName;
import com.example.orgo_project.enums.UserStatus;
import com.example.orgo_project.service.IAccountService;
import com.example.orgo_project.service.IRoleService;
import com.example.orgo_project.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private IAccountService accountService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IRoleService roleService;


    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("")
    public String showRegisterForm() {
        return "/pages/public/register";
    }

    @PostMapping
    public String registion(@RequestParam String fullName,
                            @RequestParam String email,
                            @RequestParam String username,
                            @RequestParam String phone,
                            @RequestParam String password,
                            @RequestParam String confirmPassword){
        if (phone == null || !phone.matches("^\\d{10}$")) {
            return "redirect:/register?error=InvalidPhone";
        }
        if (!password.equals(confirmPassword)) {
            return "redirect:/register?error=PasswordMismatch";
        }
        if(accountService.findByUsername(username) != null) {
            return "redirect:/register?error=UsernameExists";
        }
        if(userService.findByEmail(email) != null) {
            return "redirect:/register?error=EmailExists";
        }
        if(userService.findByPhoneNumber(phone) != null) {
            return "redirect:/register?error=PhoneExists";
        }
        Role userRole = roleService.findByRollName(RoleName.USER);
        if(userRole == null){
            userRole = new Role();
            userRole.setRoleName(RoleName.USER);
            roleService.save(userRole);
        }

        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setRole(userRole);
        accountService.save(account);


        UserProfile user = new UserProfile();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setStatus(UserStatus.ACTIVE);
        user.setAccount(account);
        userService.save(user);

        return "redirect:/login?registered";
    }
    
    
}
