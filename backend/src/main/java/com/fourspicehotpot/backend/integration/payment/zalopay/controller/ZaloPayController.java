package com.fourspicehotpot.backend.integration.payment.zalopay.controller;

import com.fourspicehotpot.backend.integration.payment.zalopay.service.ZaloPayService;
import com.fourspicehotpot.backend.modules.checkin.service.ThanhToanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(originPatterns = "http://localhost:*")
@RequestMapping("/api/admin/thanh-toan")
public class ZaloPayController {

    @Autowired
    private ZaloPayService zaloPayService;

    @Autowired
    private ThanhToanService thanhToanService;

    @PostMapping("/zalopay-callback")
    public ResponseEntity<?> zalopayCallback(@RequestBody Map<String, Object> body) {
        try {
            String maThanhToan = zaloPayService.verifyAndExtractMaThanhToan(body);
            thanhToanService.xacNhanThanhToanThanhCong(maThanhToan);

            return ResponseEntity.ok(Map.of(
                    "return_code", 1,
                    "return_message", "success"
            ));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

            if (msg.contains("invalid mac")) {
                return ResponseEntity.ok(Map.of(
                        "return_code", -1,
                        "return_message", "invalid mac"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "return_code", 0,
                    "return_message", e.getMessage()
            ));
        }
    }
}