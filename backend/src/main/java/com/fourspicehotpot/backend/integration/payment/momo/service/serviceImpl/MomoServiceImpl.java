package com.fourspicehotpot.backend.integration.payment.momo.service.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourspicehotpot.backend.integration.payment.momo.config.MomoConfig;
import com.fourspicehotpot.backend.integration.payment.momo.service.MomoService;
import com.fourspicehotpot.backend.integration.payment.momo.utils.MomoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class MomoServiceImpl implements MomoService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MomoConfig config;

    @Override
    public Map<String, Object> createPayment(String maThanhToan, BigDecimal amount) {
        try {
            String requestId = String.valueOf(System.currentTimeMillis());
            String orderId = maThanhToan;
            String orderInfo = "Thanh toan hoa don " + maThanhToan;

            String rawHash = "accessKey=" + config.getAccessKey() +
                    "&amount=" + amount.longValue() +
                    "&extraData=" +
                    "&ipnUrl=" + config.getIpnUrl() +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + config.getPartnerCode() +
                    "&redirectUrl=" + config.getRedirectUrl() +
                    "&requestId=" + requestId +
                    "&requestType=captureWallet";

            String signature = MomoUtils.hmacSHA256(config.getSecretKey(), rawHash);

            Map<String, Object> body = new HashMap<>();
            body.put("partnerCode", config.getPartnerCode());
            body.put("requestId", requestId);
            body.put("amount", amount.longValue());
            body.put("orderId", orderId);
            body.put("orderInfo", orderInfo);
            body.put("redirectUrl", config.getRedirectUrl());
            body.put("ipnUrl", config.getIpnUrl());
            body.put("requestType", "captureWallet");
            body.put("extraData", "");
            body.put("signature", signature);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(
                    config.getCreateUrl(),
                    body,
                    String.class
            );

            JsonNode json = objectMapper.readTree(response.getBody());

            if (json.get("resultCode").asInt() != 0) {
                throw new RuntimeException("MoMo lỗi: " + json.get("message").asText());
            }

            return Map.of(
                    "payUrl", json.get("payUrl").asText(),
                    "qrCodeUrl", json.get("qrCodeUrl").asText()
            );

        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo MoMo: " + e.getMessage());
        }
    }

    @Override
    public String verifyCallback(Map<String, Object> body) {
        try {
            String orderId = (String) body.get("orderId");
            Integer resultCode = (Integer) body.get("resultCode");

            if (resultCode != 0) {
                throw new RuntimeException("Thanh toán thất bại");
            }

            return orderId;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi callback MoMo");
        }
    }
}
