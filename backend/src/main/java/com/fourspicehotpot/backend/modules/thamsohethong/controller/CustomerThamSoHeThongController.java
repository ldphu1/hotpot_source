package com.fourspicehotpot.backend.modules.thamsohethong.controller;

import com.fourspicehotpot.backend.modules.thamsohethong.service.ThamSoHeThongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/public")
@CrossOrigin(originPatterns = "http://localhost:*")
public class CustomerThamSoHeThongController {
    @Autowired
    private ThamSoHeThongService thamSoHeThongService;

    @GetMapping("/hien-thi")
    public ResponseEntity<?> getHienThi() {
        return ResponseEntity.ok(thamSoHeThongService.getAllThamSo());
    }
}
