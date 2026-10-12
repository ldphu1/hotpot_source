package com.fourspicehotpot.backend.integration.payment.zalopay.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class ZaloPayUtils {

    public static String hmacSHA256(String key, String data) {
        try {
            Mac hmac256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmac256.init(secretKeySpec);
            byte[] bytes = hmac256.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo HMAC SHA256", e);
        }
    }
}