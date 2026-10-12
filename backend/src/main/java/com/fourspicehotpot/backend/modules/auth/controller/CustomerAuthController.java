package com.fourspicehotpot.backend.modules.auth.controller;

import com.fourspicehotpot.backend.modules.auth.dto.request.CustomerLoginRequest;
import com.fourspicehotpot.backend.modules.auth.service.CustomerAuthService;
import com.fourspicehotpot.backend.modules.khachhang.dto.request.KhachHangRequest;
import com.fourspicehotpot.backend.modules.khachhang.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/customer/auth")
public class CustomerAuthController {

    @Autowired
    private CustomerAuthService authCustomerService;

    @Autowired
    private KhachHangService khachHangService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody CustomerLoginRequest request) {

        return authCustomerService.login(request);

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestPart("data") KhachHangRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(khachHangService.create(req, file));
    }
}