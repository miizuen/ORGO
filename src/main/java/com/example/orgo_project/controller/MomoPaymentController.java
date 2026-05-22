package com.example.orgo_project.controller;

import com.example.orgo_project.dto.MomoCreateResponseDTO;
import com.example.orgo_project.dto.MomoIpnRequestDTO;
import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.repository.ICustomerOrderRepository;
import com.example.orgo_project.service.MomoPaymentService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/momo")
public class MomoPaymentController {

    private final MomoPaymentService momoPaymentService;
    private final ICustomerOrderRepository orderRepository;

    public MomoPaymentController(MomoPaymentService momoPaymentService,
                                 ICustomerOrderRepository orderRepository) {
        this.momoPaymentService = momoPaymentService;
        this.orderRepository = orderRepository;
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
                             Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("resultCode", resultCode);
        model.addAttribute("message", message);
        return "pages/payment/momo-return";
    }

    @PostMapping("/ipn")
    public ResponseEntity<Void> momoIpn(@RequestBody MomoIpnRequestDTO request) {
        momoPaymentService.handleIpn(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
