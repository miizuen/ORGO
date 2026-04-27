package com.example.orgo_project.controller;

import com.example.orgo_project.service.IExpertService;
import com.example.orgo_project.service.ISellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ISellerService sellerService;

    @Autowired
    private IExpertService expertService;

    @GetMapping("/dashboard")
    public String showAdminDashboard(){
        return "/pages/admin/dashboard";
    }

    @GetMapping("/seller-pending-list")
    public String showSellerPendingList(Model model){
        model.addAttribute("sellers", sellerService.getPendingList());
        return "/pages/admin/seller-pending-list";
    }

    @GetMapping("/seller-pending-list/seller-approve-detail")
    public String showSellerPendingDetail(@RequestParam int id, Model model){
        model.addAttribute("seller", sellerService.findById(id));
        return "/pages/admin/seller-approve-detail";
    }

    @PostMapping("/seller-pending-list/approve")
    public String approveSeller(@RequestParam int id){
        sellerService.approve(id);
        return "redirect:/admin/seller-pending-list?approved";
    }

    @PostMapping("/seller-pending-list/reject")
    public String rejectSeller(@RequestParam int id){
        sellerService.reject(id);
        return "redirect:/admin/seller-pending-list?rejected";
    }

    @GetMapping("/expert-pending-list")
    public String showExpertPendingList(Model model){
        model.addAttribute("experts", expertService.getPendingList());
        return "/pages/admin/expert-pending-list";
    }

    @GetMapping("/expert-pending-list/expert-approve-detail")
    public String showExpertPendingDetail(@RequestParam int id, Model model){
        model.addAttribute("expert", expertService.findById(id));
        return "/pages/admin/expert-approve-detail";
    }
    @PostMapping("/expert-pending-list/approve")
    public String approveExpert(@RequestParam int id){
        expertService.approve(id);
        return "redirect:/admin/expert-pending-list?approved";
    }

    @PostMapping("/expert-pending-list/reject")
    public String rejectExpert(@RequestParam int id){
        expertService.reject(id);
        return "redirect:/admin/expert-pending-list?rejected";
    }

}
