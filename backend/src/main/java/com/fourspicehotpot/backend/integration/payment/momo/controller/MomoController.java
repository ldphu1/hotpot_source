package com.fourspicehotpot.backend.integration.payment.momo.controller;

import com.fourspicehotpot.backend.integration.payment.momo.service.MomoService;
import com.fourspicehotpot.backend.modules.checkin.service.ThanhToanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/thanh-toan")
public class MomoController {

    @Autowired
    private MomoService momoService;

    @Autowired
    private ThanhToanService thanhToanService;

    @PostMapping("/momo")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> req) {
        String maThanhToan = (String) req.get("maThanhToan");
        BigDecimal amount = new BigDecimal(req.get("amount").toString());

        return ResponseEntity.ok(
                momoService.createPayment(maThanhToan, amount)
        );
    }

    @PostMapping("/momo-ipn")
    public ResponseEntity<?> callback(@RequestBody Map<String, Object> body) {
        String maThanhToan = momoService.verifyCallback(body);
        thanhToanService.xacNhanThanhToanThanhCong(maThanhToan);

        return ResponseEntity.ok().build();
    }
}