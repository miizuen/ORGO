package com.example.orgo_project.controller;

import com.example.orgo_project.dto.CartItemDTO;
import com.example.orgo_project.dto.CartSummaryDTO;
import com.example.orgo_project.security.CustomUserDetails;
import com.example.orgo_project.service.ICartService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final ICartService cartService;

    public CartController(ICartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String showCart(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }

        CartSummaryDTO cart = cartService.getMyCart(userDetails.getAccount().getId());
        List<CartItemDTO> items = cart.getItems();

        Map<String, List<CartItemDTO>> cartGroups = new LinkedHashMap<>();
        if (items != null) {
            cartGroups = items.stream()
                    .collect(Collectors.groupingBy(
                            item -> item.getShopName() != null ? item.getShopName() : "Nhà bán hàng",
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));
        }

        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", items);
        model.addAttribute("cartGroups", cartGroups);
        model.addAttribute("totalItems", cart.getTotalItems());
        model.addAttribute("totalAmount", cart.getTotalAmount());
        return "pages/user/cart";
    }

    @PostMapping("/add")
    public String addToCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @RequestParam Integer productVariantId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            @RequestParam(required = false) String redirectUrl,
                            RedirectAttributes redirectAttributes) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }

        try {
            cartService.addItem(userDetails.getAccount().getId(), productVariantId, quantity);
           redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:" + (redirectUrl != null && !redirectUrl.isBlank() ? redirectUrl : "/cart");
    }

    @PostMapping("/update")
    public String updateCartItem(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam Integer cartItemId,
                                 @RequestParam Integer quantity,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }

        try {
            CartItemDTO updated = cartService.updateItem(userDetails.getAccount().getId(), cartItemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage",
                    updated == null ? "Đã xóa sản phẩm khỏi giỏ hàng!" : "Đã cập nhật số lượng!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam Integer cartItemId,
                             RedirectAttributes redirectAttributes) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }

        boolean removed = cartService.removeItem(userDetails.getAccount().getId(), cartItemId);
        redirectAttributes.addFlashAttribute(
                removed ? "successMessage" : "errorMessage",
                removed ? "Đã xóa sản phẩm khỏi giỏ hàng!" : "Không tìm thấy sản phẩm trong giỏ hàng!"
        );
        return "redirect:/cart";
    }

    @PostMapping("/add-ajax")
    @ResponseBody
    public Map<String, Object> addToCartAjax(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @RequestParam Integer productVariantId,
                                             @RequestParam(defaultValue = "1") Integer quantity) {
        Map<String, Object> response = new HashMap<>();

        if (userDetails == null || userDetails.getAccount() == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập trước khi mua hàng");
            return response;
        }

        try {
            cartService.addItem(userDetails.getAccount().getId(), productVariantId, quantity);
            response.put("success", true);
            response.put("message", "🫶 Ok luôn! Đã thêm món ngon vào giỏ hàng thành công!");
        } catch (Exception ex) {
            response.put("success", false);
            response.put("message", ex.getMessage());
        }

        return response;
    }
}