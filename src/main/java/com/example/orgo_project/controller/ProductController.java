package com.example.orgo_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/products")
public class ProductController {

    @GetMapping("")
    public String showProducts() {
        return "/pages/public/product-page";
    }
    
    @GetMapping("/{id}")
    public String showProductDetail(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("productId", id);
        return "/pages/public/product-detail";
    }
    
}
