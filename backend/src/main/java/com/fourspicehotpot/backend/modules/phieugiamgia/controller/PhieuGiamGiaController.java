package com.fourspicehotpot.backend.modules.phieugiamgia.controller;

import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import com.fourspicehotpot.backend.modules.phieugiamgia.dto.request.ThemPhieuGiamGiaRequest;
import com.fourspicehotpot.backend.modules.phieugiamgia.entity.PhieuGiamGia;
import com.fourspicehotpot.backend.modules.phieugiamgia.service.PhieuGiamGiaService;
import com.fourspicehotpot.backend.modules.thucdon.dto.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RestController
@CrossOrigin(originPatterns = "http://localhost:*")
@RequestMapping("/api/admin/khuyen-mai")
public class PhieuGiamGiaController {

    @Autowired
    private PhieuGiamGiaService phieuGiamGiaService;

    @RequirePermission("XEM_PHIEU_GIAM_GIA")
    @GetMapping("/hien-thi")
    public List<PhieuGiamGia> findAllPhieuGiamGia() {

        return phieuGiamGiaService.findAllPhieuGiamGia();
    }

    @RequirePermission("THEM_PHIEU_GIAM_GIA")
    @PostMapping("/save")
    public ResponseEntity<?> savePhieuGiamGia(@Valid @RequestBody ThemPhieuGiamGiaRequest phieuGiamGia, BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    result.getFieldErrors().get(0).getDefaultMessage()
            );
        }
        try {
            phieuGiamGiaService.addPhieuGiamGia(phieuGiamGia);
            return ResponseEntity.ok(new ApiResponse(true, "Thêm phiếu giảm giá thành công", null));

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Đã sảy ra lỗi vui lòng kiểm tra lại");
        }
    }

    @RequirePermission("XEM_PHIEU_GIAM_GIA")
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getDetailPhieuGiamGia(@PathVariable Integer id) {
        try {
            Map<String, Object> phieu = phieuGiamGiaService.findPhieuGiamGiaById(id);
            return ResponseEntity.ok(phieu);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Đã sảy ra lỗi vui lòng kiểm tra lại");
        }
    }

    @RequirePermission("SUA_PHIEU_GIAM_GIA")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePhieuGiamGia(
            @PathVariable Integer id,
            @Valid @RequestBody ThemPhieuGiamGiaRequest request,
            BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    result.getFieldErrors().get(0).getDefaultMessage()
            );
        }

        try {
            phieuGiamGiaService.updatePhieuGiamGia(id, request);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Cập nhật phiếu giảm giá thành công", null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @RequirePermission("XOA_PHIEU_GIAM_GIA")
    @PutMapping("/delete/{id}")
    public ResponseEntity<?> deletePhieuGiamGia(@PathVariable Integer id) {
        phieuGiamGiaService.deletePhieuGiamGia(id);
        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật trạng thái thành công", null));
    }

    @RequirePermission("XOA_PHIEU_GIAM_GIA")
    @PutMapping("/restore/{id}")
    public ResponseEntity<?> restorePhieuGiamGia(@PathVariable Integer id) {
        phieuGiamGiaService.restorePhieuGiamGia(id);
        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật trạng thái thành công", null));
    }

    @RequirePermission("XEM_PHIEU_GIAM_GIA")
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer phuongThucGiamGia,
            @RequestParam(required = false) Integer kieuGiamGia,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        return ResponseEntity.ok(
                phieuGiamGiaService.filterPhieuGiamGia(
                        keyword,
                        phuongThucGiamGia,
                        kieuGiamGia,
                        trangThai,
                        pageable
                )
        );
    }
}
