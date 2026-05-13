package com.example.orgo_project.controller;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.Role;
import com.example.orgo_project.entity.Seller;
import com.example.orgo_project.entity.UserProfile;
import com.example.orgo_project.enums.RoleName;
import com.example.orgo_project.enums.SellerStatus;
import com.example.orgo_project.enums.UserStatus;
import com.example.orgo_project.service.IAccountService;
import com.example.orgo_project.service.IRoleService;
import com.example.orgo_project.service.ISellerService;
import com.example.orgo_project.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private IAccountService accountService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ISellerService sellerService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("")
    public String showRegisterForm() {
        return "pages/public/register";
    }

    @GetMapping("/seller")
    public String showSellerRegisterForm() {
        return "pages/public/register-seller";
    }

    @GetMapping("/register-seller")
    public String showSellerRegisterFormAlias() {
        return "pages/public/register-seller";
    }

    @GetMapping("/expert")
    public String showExpertRegisterForm() {
        return "pages/public/register";
    }

    @PostMapping("/seller")
    public String registerSeller(@RequestParam(required = false) String ownerName,
                                 @RequestParam(required = false) String ownerPhoneNumber,
                                 @RequestParam(required = false) String ownerEmail,
                                 @RequestParam(required = false) String shopName,
                                 @RequestParam(required = false) String shopEmail,
                                 @RequestParam(required = false) String shopAddress,
                                 @RequestParam(required = false) String shopDescription,
                                 @RequestParam(required = false) String taxCode,
                                 @RequestParam(required = false) MultipartFile businessRegistrationFile,
                                 @RequestParam(required = false) String bankName,
                                 @RequestParam(required = false) String bankAccount,
                                 @RequestParam(required = false) String accountHolderName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        if (username == null || "anonymousUser".equalsIgnoreCase(username)) {
            return "redirect:/login";
        }

        Account account = accountService.findByUsername(username);
        if (account == null) {
            return "redirect:/register/seller?error=AccountNotFound";
        }

        if (isBlank(ownerName) || isBlank(ownerPhoneNumber) || isBlank(ownerEmail)
                || isBlank(shopName) || isBlank(shopEmail) || isBlank(shopAddress) || isBlank(shopDescription)
                || isBlank(bankName) || isBlank(bankAccount) || isBlank(accountHolderName)) {
            return "redirect:/register/seller?error=MissingSellerInfo";
        }

        if (ownerPhoneNumber == null || !ownerPhoneNumber.matches("^\\d{10}$")) {
            return "redirect:/register/seller?error=InvalidPhone";
        }

        Seller seller = new Seller();
        seller.setShopName(shopName);
        seller.setShopAddress(shopAddress);
        seller.setTaxCode(taxCode);
        seller.setBusinessRegistrationFile(businessRegistrationFile != null && !businessRegistrationFile.isEmpty() ? businessRegistrationFile.getOriginalFilename() : null);
        seller.setOwnerName(ownerName);
        seller.setOwnerPhoneNumber(ownerPhoneNumber);
        seller.setOwnerEmail(ownerEmail);
        seller.setShopEmail(shopEmail);
        seller.setShopDescription(shopDescription);
        seller.setBankName(bankName);
        seller.setBankAccount(bankAccount);
        seller.setAccountHolderName(accountHolderName);
        seller.setStatus(SellerStatus.PENDING);
        seller.setAccount(account);
        sellerService.register(seller);
        return "redirect:/register/seller?success";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
