package com.fourspicehotpot.backend.modules.app.controller;

import com.fourspicehotpot.backend.modules.app.service.AppKhuVucService;
import com.fourspicehotpot.backend.modules.checkin.dto.request.ThemMonCheckInRequest;
import com.fourspicehotpot.backend.modules.checkin.service.CheckInService;
import com.fourspicehotpot.backend.modules.setlau.service.SetLauService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/public/menu")
public class PublicMenuController {

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private AppKhuVucService appKhuVucService;

    @Autowired
    private SetLauService setLauService;

    @GetMapping("/thuc-don/set-lau/{id}")
    public ResponseEntity<?> getSetLauById(@PathVariable("id") Integer id) {
        try {
            return ResponseEntity.ok(setLauService.findSetLauById(id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/khu-vuc")
    public ResponseEntity<?> getAllKhuVuc() {
        try {
            return ResponseEntity.ok(appKhuVucService.getDanhSachKhuVuc());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/thuc-don")
    public ResponseEntity<?> getAllThucDon() {
        try {
            return ResponseEntity.ok(checkInService.getAllThucDon());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/thuc-don/mon-le")
    public ResponseEntity<?> getAllMonLe() {
        try {
            return ResponseEntity.ok(checkInService.getAllMonLe());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/thuc-don/set-lau")
    public ResponseEntity<?> getAllSetLau() {
        try {
            return ResponseEntity.ok(checkInService.getAllSetLau());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/thuc-don/search-mon-le")
    public ResponseEntity<?> searchMonLe(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long idDanhMuc,
            @RequestParam(required = false) BigDecimal giaMin,
            @RequestParam(required = false) BigDecimal giaMax
    ) {
        try {
            return ResponseEntity.ok(checkInService.searchMonLe(keyword, idDanhMuc, giaMin, giaMax));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/thuc-don/search-set-lau")
    public ResponseEntity<?> searchSetLau(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long idNuocLau
    ) {
        try {
            return ResponseEntity.ok(checkInService.searchSetLau(keyword, idNuocLau));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/danh-sach-ban")
    public ResponseEntity<?> getDanhSachBan() {
        try {
            return ResponseEntity.ok(checkInService.getDanhSachBanCheckIn());
        } catch (Exception e) {
            e.printStackTrace(); // In toàn bộ lỗi ra cửa sổ IntelliJ Console để dễ Debug
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/them-mon")
    public ResponseEntity<?> themMonVaoHoaDon(@RequestBody ThemMonCheckInRequest request) {
        try {
            return ResponseEntity.ok(
                    checkInService.themMonVaoHoaDon(
                            request.getIdHoaDon(),
                            request.getIdSanPham(),
                            request.getSoLuong(),
                            request.getTenBan(),
                            request.getIdBanAn()
                    )
            );
        } catch (Exception e) {
            // Bắt lỗi và trả về 400 để Flutter hiện thông báo lỗi chính xác
            return ResponseEntity.badRequest().body("Không thể thêm món: " + e.getMessage());
        }
    }
}