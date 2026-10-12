package com.fourspicehotpot.backend.modules.app.controller;

import com.fourspicehotpot.backend.modules.app.service.AppDiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/discount")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicDiscountController {

    private final AppDiscountService appDiscountService;

    @GetMapping("/coupons")
    public ResponseEntity<?> getActiveCoupons() {
        try {
            return ResponseEntity.ok(appDiscountService.getActiveCoupons());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/campaigns")
    public ResponseEntity<?> getActiveCampaigns() {
        try {
            return ResponseEntity.ok(appDiscountService.getActiveCampaigns());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> getDiscountedProducts() {
        try {
            return ResponseEntity.ok(appDiscountService.getDiscountedProducts());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }
}
