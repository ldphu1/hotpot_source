package com.fourspicehotpot.backend.integration.payment.zalopay.service.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourspicehotpot.backend.integration.payment.zalopay.config.ZaloPayConfig;
import com.fourspicehotpot.backend.integration.payment.zalopay.service.ZaloPayService;
import com.fourspicehotpot.backend.integration.payment.zalopay.utils.ZaloPayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Service
public class ZaloPayServiceImpl implements ZaloPayService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ZaloPayConfig config;

    @Override
    public Map<String, Object> createInvoiceOrder(String maThanhToan, BigDecimal soTien) {
        try {
            String appId = config.getAppId();
            long appTime = System.currentTimeMillis();

            String appTransId = new SimpleDateFormat("yyMMdd").format(new Date()) + "_" + maThanhToan;

            String embedData = objectMapper.writeValueAsString(Map.of(
                    "redirecturl", config.getRedirectUrl()
            ));

            String item = "[]";
            long amount = soTien.setScale(0, java.math.RoundingMode.HALF_UP).longValue();
            String description = "Thanh toan hoa don " + maThanhToan;

            String dataToSign = appId + "|" + appTransId + "|" + "chatgptdemo" + "|" +
                    amount + "|" + appTime + "|" + embedData + "|" + item;

            String mac = ZaloPayUtils.hmacSHA256(config.getKey1(), dataToSign);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("app_id", appId);
            body.add("app_trans_id", appTransId);
            body.add("app_user", "chatgptdemo");
            body.add("app_time", String.valueOf(appTime));
            body.add("amount", String.valueOf(amount));
            body.add("description", description);
            body.add("bank_code", "");
            body.add("item", item);
            body.add("embed_data", embedData);
            body.add("callback_url", config.getCallbackUrl());
            body.add("mac", mac);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(
                    config.getCreateOrderUrl(),
                    requestEntity,
                    String.class
            );

            JsonNode json = objectMapper.readTree(response.getBody());

            if (json.path("return_code").asInt() != 1) {
                throw new RuntimeException("Tạo đơn ZaloPay thất bại: " + json.path("return_message").asText());
            }

            return Map.of(
                    "app_trans_id", appTransId,
                    "order_url", json.path("order_url").asText(""),
                    "qr_code", json.path("qr_code").asText(""),
                    "zp_trans_token", json.path("zp_trans_token").asText(""),
                    "return_code", json.path("return_code").asInt(),
                    "return_message", json.path("return_message").asText("")
            );

        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo thanh toán ZaloPay: " + e.getMessage(), e);
        }
    }

    @Override
    public String verifyAndExtractMaThanhToan(Map<String, Object> callbackBody) {
        try {
            String data = (String) callbackBody.get("data");
            String reqMac = (String) callbackBody.get("mac");

            String mac = ZaloPayUtils.hmacSHA256(config.getKey2(), data);

            if (!mac.equals(reqMac)) {
                throw new RuntimeException("invalid mac");
            }

            JsonNode dataJson = objectMapper.readTree(data);
            String appTransId = dataJson.path("app_trans_id").asText();

            if (appTransId == null || !appTransId.contains("_")) {
                throw new RuntimeException("app_trans_id không hợp lệ");
            }

            return appTransId.substring(appTransId.indexOf("_") + 1);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi verify callback ZaloPay: " + e.getMessage(), e);
        }
    }
}