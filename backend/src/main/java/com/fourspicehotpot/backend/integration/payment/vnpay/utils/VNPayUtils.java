package com.fourspicehotpot.backend.integration.payment.vnpay.utils;


import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPayUtils {


    public static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : rawHmac) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hash", e);
        }
    }


    public static String buildQuery(Map<String, String> params) {
        try {
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);

            StringBuilder query = new StringBuilder();

            for (String key : fieldNames) {
                String value = params.get(key);
                if (value != null && !value.isEmpty()) {
                    query.append(URLEncoder.encode(key, "UTF-8"));
                    query.append("=");
                    query.append(URLEncoder.encode(value, "UTF-8"));
                    query.append("&");
                }
            }

            query.deleteCharAt(query.length() - 1);
            return query.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi build query", e);
        }
    }

    public static Map<String, String> getParamsFromRequest(HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();

        Map<String, String[]> paramMap = request.getParameterMap();

        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue()[0];

            if (value != null && !value.isEmpty()) {
                result.put(key, value);
            }
        }

        return result;
    }
}
