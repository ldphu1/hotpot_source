package com.fourspicehotpot.backend.modules.khachhang.controller;

import com.fourspicehotpot.backend.modules.auth.service.KhachHangCustomerService;
import com.fourspicehotpot.backend.modules.hoadon.service.CustomerXemLichSuHoaDonService;
import com.fourspicehotpot.backend.modules.khachhang.dto.request.CustomerKhachHangRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/private")
@CrossOrigin(originPatterns = "http://localhost:*")
public class CustomerKhachHangController {

    @Autowired
    private KhachHangCustomerService khachHangCustomerService;
    @Autowired
    private CustomerXemLichSuHoaDonService customerXemLichSuHoaDonService;

    @GetMapping("/my-info")
    public ResponseEntity<?> getMyInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return ResponseEntity.ok(khachHangCustomerService.getThongTin(email));
    }


    @PutMapping("/update-info")
    public ResponseEntity<?> updateInfo(@RequestBody CustomerKhachHangRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return ResponseEntity.ok(khachHangCustomerService.updateThongTin(email, request));
    }

    @GetMapping("/lich-su-hoa-don")
    public ResponseEntity<?> getLichSuHoaDon(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(customerXemLichSuHoaDonService.getLichSu(email));
    }

    @GetMapping("/search")
    public ResponseEntity<?> getSearch(Authentication authentication, @RequestParam("maHoaDon") String maHoaDon) {
        String email = authentication.getName();
        return ResponseEntity.ok(customerXemLichSuHoaDonService.getLichSuByEmailAndMaHoaDon(email, maHoaDon));
    }

    @GetMapping("/lich-su/detail/{id}")
    public ResponseEntity<?> getLichSuDetail(@PathVariable("id") Long idHoaDon, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(customerXemLichSuHoaDonService.getChiTiet(idHoaDon, email));
    }
}
