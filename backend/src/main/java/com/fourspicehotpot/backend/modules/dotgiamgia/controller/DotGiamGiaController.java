package com.fourspicehotpot.backend.modules.dotgiamgia.controller;

import com.fourspicehotpot.backend.modules.dotgiamgia.service.DotGiamGiaService;
import com.fourspicehotpot.backend.modules.dotgiamgia.dto.request.DotGiamGiaRequest;
import com.fourspicehotpot.backend.modules.dotgiamgia.dto.response.DotGiamGiaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dot-giam-gia")
@RequiredArgsConstructor
public class DotGiamGiaController {

    private final DotGiamGiaService dotGiamGiaService;

    // ================== GET ALL ==================
    @GetMapping("/hien-thi")
    public ResponseEntity<List<DotGiamGiaResponse>> getAll() {
        return ResponseEntity.ok(dotGiamGiaService.getAll());
    }

    // ================== GET BY ID ==================
    @GetMapping("/detail/{id}")
    public ResponseEntity<DotGiamGiaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(dotGiamGiaService.getById(id));
    }

    // ================== CREATE ==================
    @PostMapping("/add")
    public ResponseEntity<DotGiamGiaResponse> create(@Valid @RequestBody DotGiamGiaRequest request) {
        return ResponseEntity.ok(dotGiamGiaService.create(request));
    }

    // ================== UPDATE ==================
    @PutMapping("/update/{id}")
    public ResponseEntity<DotGiamGiaResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DotGiamGiaRequest request
    ) {
        return ResponseEntity.ok(dotGiamGiaService.update(id, request));
    }

    // ================== DELETE (SOFT) ==================
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dotGiamGiaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ================== TOGGLE TRẠNG THÁI ==================
    @PutMapping("/doi-trang-thai/{id}")
    public ResponseEntity<DotGiamGiaResponse> doiTrangThai(@PathVariable Long id) {
        dotGiamGiaService.doiTrangThai(id);
        return ResponseEntity.ok(dotGiamGiaService.getById(id));
    }

    // ================== GET DISCOUNT BY PRODUCT ==================
    @GetMapping("/san-pham/{sanPhamId}")
    public ResponseEntity<DotGiamGiaResponse> getBySanPham(@PathVariable Long sanPhamId) {
        return ResponseEntity.ok(dotGiamGiaService.getActiveBySanPham(sanPhamId));
    }

    // ================== FILTER ==================
    @GetMapping("/filter")
    public ResponseEntity<List<DotGiamGiaResponse>> filter(
            @RequestParam(required = false) String keyword,

            @RequestParam(required = false) Integer trangThai

    ) {
        return ResponseEntity.ok(dotGiamGiaService.filter(keyword, trangThai));
    }
}
