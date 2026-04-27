package com.example.orgo_project.controller;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.Expert;
import com.example.orgo_project.entity.Seller;
import com.example.orgo_project.enums.ExpertStatus;
import com.example.orgo_project.enums.SellerStatus;
import com.example.orgo_project.service.ExpertService;
import com.example.orgo_project.service.IAccountService;
import com.example.orgo_project.service.IExpertService;
import com.example.orgo_project.service.ISellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IAccountService accountService;

    @Autowired
    private IExpertService expertService;

    @Autowired
    private ISellerService sellerService;

    @GetMapping("/register-expert")
    public String showRegisterExpertForm(){
        return "pages/user/expert-register";
    }

    @GetMapping("/register-seller")
    public String showRegisterSellerForm(){
        return "pages/user/seller-register";
    }

    @PostMapping("/register-seller")
    public String registerSeller(@RequestParam("ownerName")String ownerName,
                                 @RequestParam("ownerPhoneNumber")String ownerPhoneNumber,
                                 @RequestParam("ownerEmail")String ownerEmail,
                                 @RequestParam("shopName")String shopName,
                                 @RequestParam("shopEmail")String shopEmail,
                                 @RequestParam("shopAddress")String shopAddress,
                                 @RequestParam("shopDescription") String shopDescription,
                                 @RequestParam("taxCode")String taxCode,
                                 @RequestParam("businessRegistrationFile") MultipartFile businessRegistrationFile,
                                 Principal principal ){

        Account account = accountService.findByUsername(principal.getName());

        if (sellerService.hasApplied(account)) {
            return "redirect:/user/register-seller?error=already_applied";
        }

        String fileName = "";
        if (businessRegistrationFile != null && !businessRegistrationFile.isEmpty()) {
            fileName = businessRegistrationFile.getOriginalFilename();
        }

        Seller seller = new Seller();
        seller.setOwnerName(ownerName);
        seller.setOwnerPhoneNumber(ownerPhoneNumber);
        seller.setOwnerEmail(ownerEmail);
        seller.setShopName(shopName);
        seller.setShopEmail(shopEmail);
        seller.setShopAddress(shopAddress);
        seller.setShopDescription(shopDescription);
        seller.setTaxCode(taxCode);
        seller.setBusinessRegistrationFile(fileName);
        seller.setStatus(SellerStatus.PENDING);
        seller.setAccount(account);
        sellerService.register(seller);
        return "redirect:/user/register-seller?success";
    }

    @PostMapping("/register-expert")
    public String registerExpert(@ModelAttribute com.example.orgo_project.dto.ExpertDTO expertDTO,
                                 @RequestParam("certificateUpload")MultipartFile certificateFile,
                                 Principal principal){
        Account account = accountService.findByUsername(principal.getName());

        if (expertService.hasApplied(account)) {
            return "redirect:/user/register-expert?error=already_applied";
        }


        String fileName = "";
        if (certificateFile != null && !certificateFile.isEmpty()) {
            fileName = certificateFile.getOriginalFilename();
        }

        Expert expert = new Expert();
        expert.setExpertiseField(expertDTO.getExpertiseField());
        expert.setExperienceDescription(expertDTO.getExperienceDescription());
        expert.setCertificateFile(fileName);
        expert.setAccount(account);
        expert.setStatus(ExpertStatus.PENDING);
        expert.setCreatedAt(LocalDateTime.now());
        expertService.register(expert);
        return "redirect:/user/register-expert?success";
    }
}
