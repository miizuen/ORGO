package com.example.orgo_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/payouts")
public class PayoutController {

    @GetMapping
    public String index() {
        return "pages/admin/payouts";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id) {
        return "pages/admin/payout-detail";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Integer id) {
        return "redirect:/admin/payouts";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Integer id) {
        return "redirect:/admin/payouts";
    }

    @PostMapping("/{id}/paid")
    public String markPaid(@PathVariable Integer id) {
        return "redirect:/admin/payouts";
    }
}
