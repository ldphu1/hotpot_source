package com.fourspicehotpot.backend.integration.payment.momo.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MomoUtils {
    public static String hmacSHA256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes());

            StringBuilder hex = new StringBuilder();
            for (byte b : raw) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}