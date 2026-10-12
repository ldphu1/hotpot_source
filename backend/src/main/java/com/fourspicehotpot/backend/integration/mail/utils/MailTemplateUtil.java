package com.fourspicehotpot.backend.integration.mail.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MailTemplateUtil {
    public static String loadTemplate(String fileName) {
        try {
            ClassPathResource resource =
                    new ClassPathResource("templates/" + fileName);

            InputStream is = resource.getInputStream();
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Không tìm thấy template", e);
        }
    }
}
