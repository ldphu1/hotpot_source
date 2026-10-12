package com.fourspicehotpot.backend.modules.thongke.controller;

import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import com.fourspicehotpot.backend.modules.thongke.dto.response.TrangThaiResponse;
import com.fourspicehotpot.backend.modules.thongke.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/admin/thong-ke")
@CrossOrigin(originPatterns = "http://localhost:*")
public class ThongKeController {
    @Autowired
    ThongKeService thongKeService;


    @GetMapping("/trang-thai-hoa-don")
    public ResponseEntity<TrangThaiResponse> getTrangThaiHoaDon() {
        return ResponseEntity.ok(
                thongKeService.getTrangThai()
        );
    }

    @RequirePermission("XEM_HOA_DON")
    @RequestMapping("/doanh-thu")
    public ResponseEntity<?> layDoanhThu() {
        return ResponseEntity.ok(thongKeService.layThongKeDoanhThu());
    }

    @RequirePermission("XEM_HOA_DON")
    @GetMapping("/tong-quan")
    public ResponseEntity<?> getTongQuan(
            @RequestParam String type // HOM_NAY | TUAN | THANG | NAM
    ) {

        LocalDateTime from;
        LocalDateTime to = LocalDateTime.now().with(LocalTime.MAX);

        switch (type) {
            case "HOM_NAY":
                from = LocalDate.now().atStartOfDay();
                break;

            case "TUAN":
                from = LocalDate.now()
                        .with(DayOfWeek.MONDAY)
                        .atStartOfDay();
                break;

            case "THANG":
                from = LocalDate.now().withDayOfMonth(1).atStartOfDay();
                break;

            case "NAM":
                from = LocalDate.now().withDayOfYear(1).atStartOfDay();
                break;

            default:
                throw new IllegalArgumentException("Invalid type");
        }

        return ResponseEntity.ok(
                thongKeService.layThongKeTongQuan(from, to)
        );
    }

    @GetMapping("/doanh-thu-thang")
    public ResponseEntity<?> getDoanhThuTheoThang() {
        return ResponseEntity.ok(
                thongKeService.thongKeDoanhThuTheoThang()
        );
    }

    @GetMapping("/top-set")
    public ResponseEntity<?> getTopSet() {
        return ResponseEntity.ok(thongKeService.getTopSet());
    }

    @GetMapping("/booking-channel")
    public ResponseEntity<?> getBookingChannel() {
        return ResponseEntity.ok(thongKeService.getBookingChannel());
    }

}
