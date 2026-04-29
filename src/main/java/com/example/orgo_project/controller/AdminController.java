package com.example.orgo_project.controller;

import com.example.orgo_project.service.IExpertService;
import com.example.orgo_project.service.ISellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.orgo_project.dto.ExpertDTO;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ISellerService sellerService;

    @Autowired
    private IExpertService expertService;

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model){
        model.addAttribute("activePage", "dashboard");
        return "/pages/admin/dashboard";
    }

    @GetMapping("/seller-pending-list")
    public String showSellerPendingList(Model model){
        model.addAttribute("activePage", "seller-pending-list");
        model.addAttribute("sellers", sellerService.getPendingList());
        return "/pages/admin/seller-pending-list";
    }

    @GetMapping("/seller-pending-list/seller-approve-detail")
    public String showSellerPendingDetail(@RequestParam int id, Model model){
        model.addAttribute("activePage", "seller-pending-list");
        model.addAttribute("seller", sellerService.findById(id));
        return "/pages/admin/seller-approve-detail";
    }

    @PostMapping("/seller-pending-list/approve")
    public String approveSeller(@RequestParam int id, Model model){
        sellerService.approve(id);
        model.addAttribute("successMessage", "Đã phê duyệt thành công!");
        return "redirect:/admin/seller-pending-list?approved";
    }

    @PostMapping("/seller-pending-list/reject")
    public String rejectSeller(@RequestParam int id, Model model){
        sellerService.reject(id);
        model.addAttribute("successMessage", "Đã từ chối thành công!");
        return "redirect:/admin/seller-pending-list?rejected";
    }

    @GetMapping("/expert-pending-list")
    public String showExpertPendingList(Model model){
        model.addAttribute("activePage", "expert-pending-list");
        model.addAttribute("experts", expertService.getPendingList().stream().map(ExpertDTO::fromEntity).collect(Collectors.toList()));
        return "/pages/admin/expert-pending-list";
    }

    @GetMapping("/expert-pending-list/expert-approve-detail")
    public String showExpertPendingDetail(@RequestParam int id, Model model){
        model.addAttribute("activePage", "expert-pending-list");
        model.addAttribute("expert", ExpertDTO.fromEntity(expertService.findById(id)));
        return "/pages/admin/expert-approve-detail";
    }
    @PostMapping("/expert-pending-list/approve")
    public String approveExpert(@RequestParam int id, Model model){
        expertService.approve(id);
        model.addAttribute("successMessage", "Đã phê duyệt thành công!");
        return "redirect:/admin/expert-pending-list?approved";
    }

    @PostMapping("/expert-pending-list/reject")
    public String rejectExpert(@RequestParam int id, Model model){
        expertService.reject(id);
        model.addAttribute("successMessage", "Đã từ chối thành công!");
        return "redirect:/admin/expert-pending-list?rejected";
    }

}
