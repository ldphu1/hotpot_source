package com.fourspicehotpot.backend.modules.khachhang.controller;


import com.fourspicehotpot.backend.modules.khachhang.service.DiaChiKhachHangService;
import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import com.fourspicehotpot.backend.modules.khachhang.dto.request.DiaChiKhachHangRequest;
import com.fourspicehotpot.backend.modules.khachhang.dto.response.DiaChiKhachHangResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(originPatterns = "http://localhost:*")
@RequestMapping("/api/admin/dia-chi-khach-hang")
public class DiaChiKhachHangControler {
    @Autowired
    private DiaChiKhachHangService diaChiService;

    @RequirePermission("XEM_DIA_CHI_KHACH")
    @GetMapping("/{khId}")
    public List<DiaChiKhachHangResponse> getByKhachHang(@PathVariable Long khId) {
        return diaChiService.getByKhachHangId(khId);
    }

    @RequirePermission("THEM_DIA_CHI_KHACH")
    @PostMapping("/{khId}")
    public DiaChiKhachHangResponse add(
            @PathVariable Long khId,
            @RequestBody @Valid DiaChiKhachHangRequest req) {

        return diaChiService.add(khId, req);
    }


    @RequirePermission("DAT_DIA_CHI_MAC_DINH")
    @PutMapping("/mac-dinh/{diaChiId}")
    public ResponseEntity<?> setMacDinh(@PathVariable Long diaChiId) {

        diaChiService.setMacDinh(diaChiId);

        return ResponseEntity.ok("Đặt mặc định thành công");
    }

    @RequirePermission("SUA_DIA_CHI_KHACH")
    @PutMapping("/{diaChiId}")
    public DiaChiKhachHangResponse update(
            @PathVariable Long diaChiId,
            @RequestBody @Valid DiaChiKhachHangRequest req) {

        return diaChiService.update(diaChiId, req);
    }
}
