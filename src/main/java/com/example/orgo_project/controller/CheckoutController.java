package com.example.orgo_project.controller;

import com.example.orgo_project.dto.CheckoutRequestDTO;
import com.example.orgo_project.entity.PaymentQrSession;
import com.example.orgo_project.security.CustomUserDetails;
import com.example.orgo_project.service.ICheckoutService;
import com.example.orgo_project.service.PaymentBankConfigService;
import com.example.orgo_project.service.PaymentQrService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final ICheckoutService checkoutService;
    private final PaymentQrService paymentQrService;
    private final PaymentBankConfigService paymentBankConfigService;

    public CheckoutController(ICheckoutService checkoutService, PaymentQrService paymentQrService, PaymentBankConfigService paymentBankConfigService) {
        this.checkoutService = checkoutService;
        this.paymentQrService = paymentQrService;
        this.paymentBankConfigService = paymentBankConfigService;
    }

    @GetMapping
    public String checkoutPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam(required = false) String selectedItemIds,
                               Model model) {
        if (userDetails == null || userDetails.getAccount() == null) return "redirect:/login";
        var checkoutData = checkoutService.getCheckoutPageData(userDetails.getAccount().getId(), selectedItemIds);
        model.addAttribute("cartItems", checkoutData.getItems());
        model.addAttribute("totalAmount", checkoutData.getTotalAmount());
        model.addAttribute("selectedItemIds", selectedItemIds);
        model.addAttribute("shippingAddresses", checkoutData.getShippingAddresses());
        model.addAttribute("defaultShippingAddress", checkoutData.getDefaultShippingAddress());
        model.addAttribute("bankConfig", paymentBankConfigService.getActiveConfig());
        return "pages/user/checkout";
    }

    @PostMapping
    public String placeOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @ModelAttribute CheckoutRequestDTO request,
                             @RequestParam(required = false) String selectedItemIds,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (userDetails == null || userDetails.getAccount() == null) return "redirect:/login";
        try {
            Integer articleId = (Integer) session.getAttribute("articleId");
            session.removeAttribute("articleId");
            var response = checkoutService.checkout(userDetails.getAccount().getId(), request, selectedItemIds, articleId);
            redirectAttributes.addFlashAttribute("checkoutResult", response);
            redirectAttributes.addAttribute("orderId", response.getOrderId());
            redirectAttributes.addAttribute("orderCode", response.getOrderCode());
            redirectAttributes.addAttribute("totalAmount", response.getTotalAmount());
            return "redirect:/checkout/success";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/success")
    public String successPage(@RequestParam(required = false) Integer orderId,
                              @RequestParam(required = false) String orderCode,
                              @RequestParam(required = false) String totalAmount,
                              Model model) {
        model.addAttribute("orderCode", orderCode);
        model.addAttribute("totalAmount", totalAmount);

        PaymentQrSession paymentQrSession = orderId != null ? paymentQrService.findByOrderId(orderId) : null;
        model.addAttribute("paymentQrSession", paymentQrSession);
        model.addAttribute("bankConfig", paymentBankConfigService.getActiveConfig());
        model.addAttribute("paymentQrImageUrl", paymentQrSession != null ? buildVietQrImageUrl(paymentQrSession) : null);
        return "pages/user/checkout-success";
    }

    @PostMapping("/{orderId}/confirm-payment")
    public String confirmPayment(@PathVariable Integer orderId,
                                 @RequestParam(required = false) String transactionCode,
                                 RedirectAttributes redirectAttributes) {
        try {
            var response = checkoutService.confirmPayment(orderId, transactionCode);
            redirectAttributes.addFlashAttribute("checkoutResult", response);
            redirectAttributes.addAttribute("orderId", response.getOrderId());
            redirectAttributes.addAttribute("orderCode", response.getOrderCode());
            redirectAttributes.addAttribute("totalAmount", response.getTotalAmount());
            return "redirect:/checkout/success";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/checkout/success?orderId=" + orderId;
        }
    }

    @GetMapping("/qr/{orderId}")
    @ResponseBody
    public byte[] qrImage(@PathVariable Integer orderId) { return QrCodeGenerator.generatePng("ORGO-QR-" + orderId, 280, 280); }

    private String buildVietQrImageUrl(PaymentQrSession session) {
        String bank = normalizeBankCode(session.getBankName());
        String accountNo = safe(session.getAccountNumber());
        String amount = session.getAmount() != null ? session.getAmount().toPlainString() : "0";
        String addInfo = urlEncode(session.getTransferContent() != null ? session.getTransferContent() : String.valueOf(session.getOrderId()));
        String accountName = urlEncode(session.getAccountHolderName() != null ? session.getAccountHolderName() : "NGUYEN HA VI");
        return "https://img.vietqr.io/image/" + bank + "-" + accountNo + "-compact2.png?amount=" + amount + "&addInfo=" + addInfo + "&accountName=" + accountName;
    }

    private String normalizeBankCode(String bankName) {
        if (bankName == null || bankName.isBlank()) return "mb";
        String normalized = bankName.trim().toLowerCase();
        return switch (normalized) {
            case "vietcombank", "vcb" -> "vcb";
            case "mb", "mb bank", "military bank" -> "mb";
            case "techcombank", "tcb" -> "tcb";
            case "acb" -> "acb";
            case "bidv" -> "bidv";
            case "agribank" -> "agribank";
            default -> normalized.replaceAll("\\s+", "");
        };
    }

    private String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    private String safe(String value) { return value == null ? "" : value; }
}
