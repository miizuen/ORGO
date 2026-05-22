package com.example.orgo_project.service;

import com.example.orgo_project.config.MomoProperties;
import com.example.orgo_project.dto.MomoCreateRequestDTO;
import com.example.orgo_project.dto.MomoCreateResponseDTO;
import com.example.orgo_project.dto.MomoIpnRequestDTO;
import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.entity.PaymentHistory;
import com.example.orgo_project.enums.OrderStatus;
import com.example.orgo_project.enums.PaymentStatus;
import com.example.orgo_project.repository.ICustomerOrderRepository;
import com.example.orgo_project.repository.IPaymentHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@Transactional
public class MomoPaymentServiceImpl implements MomoPaymentService {

    private static final Logger log = LoggerFactory.getLogger(MomoPaymentServiceImpl.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final ICustomerOrderRepository orderRepository;
    private final IPaymentHistoryRepository paymentHistoryRepository;
    private final IRevenueDistributionService revenueDistributionService;
    private final MomoProperties momoProperties;

    public MomoPaymentServiceImpl(RestClient.Builder restClientBuilder,
                                  ObjectMapper objectMapper,
                                  ICustomerOrderRepository orderRepository,
                                  IPaymentHistoryRepository paymentHistoryRepository,
                                  IRevenueDistributionService revenueDistributionService,
                                  MomoProperties momoProperties) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
        this.paymentHistoryRepository = paymentHistoryRepository;
        this.revenueDistributionService = revenueDistributionService;
        this.momoProperties = momoProperties;
    }

    @Override
    public MomoCreateResponseDTO createPayment(Integer orderId, String orderCode, String orderInfo, long amount) {
        String requestId = String.valueOf(System.currentTimeMillis());
        String extraData = "";
        String raw = buildCreateSignature(amount, extraData, orderCode, orderInfo, requestId);
        String signature = hmacSha256(raw, momoProperties.getSecretKey());

        log.info("MoMo create payment: orderId={}, orderCode={}, requestId={}, amount={}, endpoint={}, redirectUrl={}, ipnUrl={}",
                orderId, orderCode, requestId, amount, momoProperties.getEndpointCreate(), momoProperties.getRedirectUrl(), momoProperties.getIpnUrl());
        log.debug("MoMo create signature raw: {}", raw);

        MomoCreateRequestDTO req = MomoCreateRequestDTO.builder()
                .partnerCode(momoProperties.getPartnerCode())
                .partnerName("ORGO")
                .storeId(momoProperties.getPartnerCode())
                .requestId(requestId)
                .amount(amount)
                .orderId(orderCode)
                .orderInfo(orderInfo)
                .redirectUrl(momoProperties.getRedirectUrl())
                .ipnUrl(momoProperties.getIpnUrl())
                .requestType(momoProperties.getRequestType())
                .extraData(extraData)
                .lang(momoProperties.getLang())
                .signature(signature)
                .build();

        try {
            String response = restClient.post()
                    .uri(momoProperties.getEndpointCreate())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(req)
                    .retrieve()
                    .body(String.class);

            log.info("MoMo create payment response received: orderCode={}, requestId={}, rawResponse={}", orderCode, requestId, response);
            return objectMapper.readValue(response, MomoCreateResponseDTO.class);
        } catch (Exception e) {
            log.error("MoMo create payment failed: orderCode={}, requestId={}, amount={}, message={}", orderCode, requestId, amount, e.getMessage(), e);
            throw new RuntimeException("Không tạo được giao dịch MoMo: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean handleIpn(MomoIpnRequestDTO request) {
        if (request == null || request.getPartnerCode() == null || request.getOrderId() == null) {
            log.warn("MoMo IPN rejected: missing request, partnerCode or orderId");
            return false;
        }
        if (!request.getPartnerCode().equals(momoProperties.getPartnerCode())) {
            log.warn("MoMo IPN rejected: partnerCode mismatch. requestPartnerCode={}, configuredPartnerCode={}, orderId={}",
                    request.getPartnerCode(), momoProperties.getPartnerCode(), request.getOrderId());
            return false;
        }

        String raw = buildIpnSignature(request);
        String expected = hmacSha256(raw, momoProperties.getSecretKey());
        if (!expected.equalsIgnoreCase(request.getSignature())) {
            log.warn("MoMo IPN rejected: invalid signature. orderId={}, requestId={}, transId={}, resultCode={}",
                    request.getOrderId(), request.getRequestId(), request.getTransId(), request.getResultCode());
            log.debug("MoMo IPN signature raw: {}", raw);
            return false;
        }

        CustomerOrder order = orderRepository.findByOrderCode(request.getOrderId()).orElse(null);
        if (order == null) {
            log.warn("MoMo IPN rejected: order not found. orderId={}, requestId={}, transId={}",
                    request.getOrderId(), request.getRequestId(), request.getTransId());
            return false;
        }

        PaymentStatus mappedStatus = mapPaymentStatus(request.getResultCode());
        String transactionCode = request.getTransId() > 0 ? String.valueOf(request.getTransId()) : "MOMO-" + order.getOrderCode();

        PaymentHistory existingHistory = paymentHistoryRepository.findByTransactionCode(transactionCode).orElse(null);
        if (existingHistory != null) {
            log.info("MoMo IPN ignored: duplicate transaction. orderCode={}, transactionCode={}, resultCode={}",
                    order.getOrderCode(), transactionCode, request.getResultCode());
            return true;
        }

        if (mappedStatus == PaymentStatus.PAID && order.getPaymentStatus() == PaymentStatus.PAID) {
            log.info("MoMo IPN ignored: order already paid. orderCode={}, transactionCode={}, requestId={}",
                    order.getOrderCode(), transactionCode, request.getRequestId());
            return true;
        }

        log.info("MoMo IPN accepted: orderCode={}, transactionCode={}, resultCode={}, mappedStatus={}, amount={}, requestId={}",
                order.getOrderCode(), transactionCode, request.getResultCode(), mappedStatus, request.getAmount(), request.getRequestId());

        order.setPaymentStatus(mappedStatus);
        if (mappedStatus == PaymentStatus.PAID) {
            order.setPaidAt(LocalDateTime.now());
            if (order.getOrderStatus() == null || order.getOrderStatus() == OrderStatus.PENDING) {
                order.setOrderStatus(OrderStatus.PENDING);
            }
        } else if (mappedStatus == PaymentStatus.FAILED) {
            if (order.getOrderStatus() == null || order.getOrderStatus() == OrderStatus.PENDING) {
                order.setOrderStatus(OrderStatus.CANCELLED);
            }
        }
        orderRepository.save(order);

        savePaymentHistory(order, request, mappedStatus, transactionCode);

        if (mappedStatus == PaymentStatus.PAID) {
            log.info("MoMo revenue distribution started: orderId={}, orderCode={}, transactionCode={}", order.getId(), order.getOrderCode(), transactionCode);
            revenueDistributionService.distributeForOrder(order.getId());
        }
        return true;
    }

    private String buildCreateSignature(long amount, String extraData, String orderCode, String orderInfo, String requestId) {
        return "accessKey=" + momoProperties.getAccessKey() +
                "&amount=" + amount +
                "&extraData=" + safe(extraData) +
                "&ipnUrl=" + momoProperties.getIpnUrl() +
                "&orderId=" + orderCode +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + momoProperties.getPartnerCode() +
                "&redirectUrl=" + momoProperties.getRedirectUrl() +
                "&requestId=" + requestId +
                "&requestType=" + momoProperties.getRequestType();
    }

    private String buildIpnSignature(MomoIpnRequestDTO request) {
        return "accessKey=" + momoProperties.getAccessKey() +
                "&amount=" + request.getAmount() +
                "&extraData=" + safe(request.getExtraData()) +
                "&message=" + safe(request.getMessage()) +
                "&orderId=" + request.getOrderId() +
                "&orderInfo=" + safe(request.getOrderInfo()) +
                "&orderType=" + safe(request.getOrderType()) +
                "&partnerCode=" + request.getPartnerCode() +
                "&payType=" + safe(request.getPayType()) +
                "&requestId=" + request.getRequestId() +
                "&responseTime=" + request.getResponseTime() +
                "&resultCode=" + request.getResultCode() +
                "&transId=" + request.getTransId();
    }

    private PaymentStatus mapPaymentStatus(int resultCode) {
        if (resultCode == 0) return PaymentStatus.PAID;
        if (resultCode == 1006 || resultCode == 1008 || resultCode == 1009 || resultCode == 1016) {
            return PaymentStatus.PENDING;
        }
        return PaymentStatus.FAILED;
    }

    private void savePaymentHistory(CustomerOrder order, MomoIpnRequestDTO request, PaymentStatus status, String transactionCode) {
        PaymentHistory history = new PaymentHistory();
        history.setOrderId(order.getId());
        history.setPaymentMethodId(order.getPaymentMethodId());
        history.setTransactionCode(transactionCode);
        history.setAmount(request.getAmount() > 0 ? BigDecimal.valueOf(request.getAmount()) : order.getTotalAmount());
        history.setStatus(status);
        history.setTransactionAt(LocalDateTime.now());
        history.setNote("MoMo IPN: " + safe(request.getMessage()));
        paymentHistoryRepository.save(history);
        log.info("MoMo payment history saved: orderCode={}, transactionCode={}, status={}, amount={}",
                order.getOrderCode(), transactionCode, status, history.getAmount());
    }

    private String safe(String value) { return value == null ? "" : value; }

    private String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            log.error("MoMo signature generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Không tạo được chữ ký MoMo", e);
        }
    }
}
