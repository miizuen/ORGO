package com.example.orgo_project.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.orgo_project.dto.MomoCreateResponseDTO;
import com.example.orgo_project.dto.MomoIpnRequestDTO;
import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.enums.PaymentStatus;
import com.example.orgo_project.repository.ICustomerOrderRepository;
import com.example.orgo_project.service.ICheckoutService;
import com.example.orgo_project.service.MomoPaymentService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Controller
@RequestMapping("/momo")
public class MomoPaymentController {

    private final MomoPaymentService momoPaymentService;
    private final ICustomerOrderRepository orderRepository;
    private final ICheckoutService checkoutService;

    public MomoPaymentController(MomoPaymentService momoPaymentService,
                                 ICustomerOrderRepository orderRepository,
                                 ICheckoutService checkoutService) {
        this.momoPaymentService = momoPaymentService;
        this.orderRepository = orderRepository;
        this.checkoutService = checkoutService;
    }

    @GetMapping("/demo")
    public String demoPage(Model model) {
        model.addAttribute("amount", 1000);
        model.addAttribute("orderInfo", "Demo thanh toan MoMo");
        return "pages/payment/momo-demo";
    }

    @PostMapping("/demo/create")
    public String createDemoPayment(@RequestParam @Min(1000) long amount,
                                    @RequestParam @NotBlank String orderInfo,
                                    RedirectAttributes redirectAttributes) {
        String orderCode = "ORD-" + System.currentTimeMillis();
        CustomerOrder order = new CustomerOrder();
        order.setOrderCode(orderCode);
        order.setTotalAmount(BigDecimal.valueOf(amount));
        order = orderRepository.save(order);

        MomoCreateResponseDTO response = momoPaymentService.createPayment(order.getId(), order.getOrderCode(), orderInfo, amount);
        redirectAttributes.addFlashAttribute("order", order);
        redirectAttributes.addFlashAttribute("response", response);
        return "redirect:/momo/demo/result";
    }

    @GetMapping("/demo/result")
    public String demoResultPage(@ModelAttribute("order") CustomerOrder order,
                                 @ModelAttribute("response") MomoCreateResponseDTO response,
                                 Model model) {
        if (order != null) {
            model.addAttribute("order", order);
        }
        if (response != null) {
            model.addAttribute("response", response);
        }
        return "pages/payment/momo-demo-result";
    }

    @GetMapping("/return")
    public String momoReturn(@RequestParam(required = false) String orderId,
                             @RequestParam(required = false) String resultCode,
                             @RequestParam(required = false) String message,
                             @RequestParam(required = false) String transId,
                             @RequestParam(required = false) String amount,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        System.out.println("========== MOMO RETURN ==========");
        System.out.println("Order ID: " + orderId);
        System.out.println("Result Code: " + resultCode);
        System.out.println("Trans ID: " + transId);
        System.out.println("Message: " + message);
        System.out.println("=================================");

        // Fallback for local/dev where IPN may not reach the server.
        if ("0".equals(resultCode) && orderId != null) {
            try {
                CustomerOrder order = orderRepository.findByOrderCode(orderId).orElse(null);
                if (order != null) {
                    if (order.getPaymentStatus() != PaymentStatus.PAID) {
                        checkoutService.confirmPayment(order.getId(), transId);
                        System.out.println("Order payment confirmed and revenue distributed: " + orderId);
                    }
                    redirectAttributes.addFlashAttribute("successMessage", "Thanh toan thanh cong!");
                    return "redirect:/orders/" + order.getId();
                }
            } catch (Exception e) {
                System.err.println("Error confirming order payment: " + e.getMessage());
            }
        }

        model.addAttribute("orderId", orderId);
        model.addAttribute("resultCode", resultCode);
        model.addAttribute("message", message);
        return "pages/payment/momo-return";
    }

    @PostMapping("/ipn")
    public ResponseEntity<Void> momoIpn(@RequestBody MomoIpnRequestDTO request) {
        System.out.println("========== RECEIVED MOMO IPN ==========");
        System.out.println("Order ID: " + request.getOrderId());
        System.out.println("Result Code: " + request.getResultCode());
        System.out.println("Message: " + request.getMessage());
        System.out.println("=======================================");

        momoPaymentService.handleIpn(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
