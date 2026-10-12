package com.fourspicehotpot.backend.modules.khachhang.controller;

import com.fourspicehotpot.backend.modules.khachhang.service.KhachHangService;
import com.fourspicehotpot.backend.common.exporter.KhachHangExportService;
import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import com.fourspicehotpot.backend.modules.khachhang.dto.request.KhachHangRequest;
import com.fourspicehotpot.backend.modules.khachhang.dto.response.KhachHangResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@CrossOrigin(originPatterns = "http://localhost:*")
@RequestMapping("/api/admin/khach-hang")
public class KhachHangController {
    @Autowired
    KhachHangService khachHangService;
    @Autowired
    KhachHangExportService khachHangExportService;


    @RequirePermission(("XEM_KHACH_HANG"))
    @GetMapping("/hien-thi")
    public ResponseEntity<List<KhachHangResponse>> hienThi() {
        return ResponseEntity.ok(khachHangService.getAllKhachHang());
    }

    @RequirePermission("THEM_KHACH_HANG")
    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<KhachHangResponse> create(
            @RequestPart("data") @Valid KhachHangRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(khachHangService.create(req, file));
    }

    @RequirePermission("SUA_KHACH_HANG")
    @PutMapping("/{id}/doi-trang-thai")
    public ResponseEntity<Void> doiTrangThai(@PathVariable Long id) {
        khachHangService.doiTrangThai(id);
        return ResponseEntity.ok().build();
    }

    @RequirePermission("SUA_KHACH_HANG")
    @PostMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestPart("data") @Valid KhachHangRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        khachHangService.update(id, req, file);
        return ResponseEntity.ok("OK");
    }

    @RequirePermission("XEM_KHACH_HANG")
    @GetMapping("/{id}")
    public ResponseEntity<KhachHangResponse> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(khachHangService.getById(id));
    }

    // export chọn
    @RequirePermission("XEM_KHACH_HANG")
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportSelected(@RequestBody List<Long> ids) throws Exception {
        byte[] file = khachHangExportService.exportExcelByIds(ids);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=khach-hang.xlsx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(file);
    }

    @GetMapping("/tim-kiem")
    public ResponseEntity<?> search(@RequestParam String keyword) {
        System.out.println("KEYWORD = [" + keyword + "]");
        return ResponseEntity.ok(khachHangService.search(keyword));
    }


}
