package com.fourspicehotpot.backend.modules.giohang.controller;


import com.fourspicehotpot.backend.modules.giohang.dto.request.GioHangRequest;
import com.fourspicehotpot.backend.modules.giohang.dto.response.GioHangResponse;
import com.fourspicehotpot.backend.modules.giohang.service.GioHangService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/gio-hang")
@CrossOrigin(originPatterns = "http://localhost:*")
public class CustomerGioHangController {

    @Autowired
    private GioHangService gioHangService;

    @PostMapping("/them-vao-gio")
    public ResponseEntity<?> saveThemVaoGio(@RequestBody GioHangRequest request, HttpServletRequest servletRequest) {
        GioHangResponse response = gioHangService.themVaoGioHang(request, servletRequest);
        return ResponseEntity.ok(response);
    }


}
