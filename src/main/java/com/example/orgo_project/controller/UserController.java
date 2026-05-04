package com.example.orgo_project.controller;

import com.example.orgo_project.dto.ChangePasswordDTO;
import com.example.orgo_project.dto.ShippingAddressDTO;
import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.Expert;
import com.example.orgo_project.entity.Seller;
import com.example.orgo_project.entity.ShippingAddress;
import com.example.orgo_project.entity.UserProfile;
import com.example.orgo_project.enums.ExpertStatus;
import com.example.orgo_project.enums.SellerStatus;
import com.example.orgo_project.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IAccountService accountService;

    @Autowired
    private IExpertService expertService;

    @Autowired
    private ISellerService sellerService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IShippingAddressService shippingAddressService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public String showProfile(Model model){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Account account = accountService.findByUsername(username);
        UserProfile userProfile = account != null ? account.getUser() : null;
        if (userProfile == null && account != null) {
            userProfile = new UserProfile();
            userProfile.setAccount(account);
            userProfile.setEmail(account.getUsername());
        }

        model.addAttribute("userProfile", userProfile);
        model.addAttribute("account", account);
        return "pages/user/profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam String fullName,
                                RedirectAttributes redirectAttributes){
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            Account account = accountService.findByUsername(username);
            if (account == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản!");
                return "redirect:/user/profile";
            }

            UserProfile userProfile = account.getUser();
            if (userProfile == null) {
                userProfile = new UserProfile();
                userProfile.setAccount(account);
            }

            String normalizedFullName = fullName != null ? fullName.trim() : "";
            String normalizedEmail = email != null ? email.trim() : "";
            String normalizedPhone = phone != null ? phone.trim() : "";

            if (normalizedFullName.isBlank()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Họ tên không được để trống!");
                return "redirect:/user/profile";
            }
            if (normalizedEmail.isBlank()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email không được để trống!");
                return "redirect:/user/profile";
            }

            userProfile.setEmail(normalizedEmail);
            userProfile.setPhoneNumber(normalizedPhone);
            userProfile.setFullName(normalizedFullName);
            userProfile.setAccount(account);
            userService.save(userProfile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật thất bại, vui lòng thử lại!");
        }
        return "redirect:/user/profile";
    }

    @GetMapping("/change-password")
    public String showChangePassword(Model model) {
        model.addAttribute("changePasswordDTO", new ChangePasswordDTO());
        return "pages/user/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute ChangePasswordDTO changePasswordDTO,
                                 RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountService.findByUsername(auth.getName());

        if (account == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản!");
            return "redirect:/user/change-password";
        }

        if (changePasswordDTO.getCurrentPassword() == null || changePasswordDTO.getCurrentPassword().isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập mật khẩu cũ!");
            return "redirect:/user/change-password";
        }

        if (changePasswordDTO.getNewPassword() == null || changePasswordDTO.getNewPassword().length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return "redirect:/user/change-password";
        }

        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "redirect:/user/change-password";
        }

        boolean changed = accountService.changePassword(account, changePasswordDTO.getCurrentPassword(), changePasswordDTO.getNewPassword());
        if (changed) {
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
            return "redirect:/user/profile";
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu cũ không đúng!");
        return "redirect:/user/change-password";
    }

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

    @GetMapping("/addresses")
    public String showAddresses(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountService.findByUsername(auth.getName());
        List<ShippingAddress> addresses = shippingAddressService.findByAccountId(account.getId());
        model.addAttribute("addresses", addresses.stream().map(ShippingAddressDTO::fromEntity).toList());
        model.addAttribute("shippingAddressDTO", new ShippingAddressDTO());
        model.addAttribute("account", account);
        return "pages/user/addresses";
    }

    @PostMapping("/addresses")
    public String saveAddress(@ModelAttribute ShippingAddressDTO shippingAddressDTO,
                              RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountService.findByUsername(auth.getName());

        ShippingAddress address = new ShippingAddress();
        address.setId(shippingAddressDTO.getId());
        address.setAccountId(account.getId());
        address.setRecipientName(shippingAddressDTO.getRecipientName());
        address.setRecipientPhone(shippingAddressDTO.getRecipientPhone());
        address.setProvinceOrCity(shippingAddressDTO.getProvinceOrCity());
        address.setDefaultAddress(shippingAddressDTO.getDefaultAddress() != null && shippingAddressDTO.getDefaultAddress());
        address.setAddressType(shippingAddressDTO.getAddressType());
        address.setDetailedAddress(shippingAddressDTO.getDetailedAddress());

        if (Boolean.TRUE.equals(address.getDefaultAddress())) {
            shippingAddressService.findByAccountId(account.getId()).forEach(existing -> {
                if (!existing.getId().equals(address.getId())) {
                    existing.setDefaultAddress(false);
                    shippingAddressService.update(existing);
                }
            });
        }

        if (address.getId() == null) {
            shippingAddressService.save(address);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm địa chỉ giao hàng!");
        } else {
            shippingAddressService.update(address);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật địa chỉ giao hàng!");
        }
        return "redirect:/user/addresses";
    }

    @GetMapping("/addresses/edit/{id}")
    public String editAddress(@PathVariable Integer id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountService.findByUsername(auth.getName());
        ShippingAddress address = shippingAddressService.findById(id);
        if (address == null || !address.getAccountId().equals(account.getId())) {
            return "redirect:/user/addresses";
        }
        model.addAttribute("addresses", shippingAddressService.findByAccountId(account.getId()).stream().map(ShippingAddressDTO::fromEntity).toList());
        model.addAttribute("shippingAddressDTO", ShippingAddressDTO.fromEntity(address));
        model.addAttribute("account", account);
        return "pages/user/addresses";
    }

    @PostMapping("/addresses/delete/{id}")
    public String deleteAddress(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountService.findByUsername(auth.getName());
        boolean deleted = shippingAddressService.delete(id, account.getId());
        redirectAttributes.addFlashAttribute(deleted ? "successMessage" : "errorMessage",
                deleted ? "Đã xóa địa chỉ giao hàng!" : "Không thể xóa địa chỉ này!");
        return "redirect:/user/addresses";
    }
}
