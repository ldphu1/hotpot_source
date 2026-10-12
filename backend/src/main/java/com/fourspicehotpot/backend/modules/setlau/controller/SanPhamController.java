package com.fourspicehotpot.backend.modules.setlau.controller;


import com.fourspicehotpot.backend.modules.setlau.service.SanPhamService;
import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import com.fourspicehotpot.backend.modules.setlau.dto.request.SanPhamListRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.request.SanPhamRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.request.SanPhamSearchRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/san-pham")
@CrossOrigin(originPatterns = "http://localhost:*")
public class SanPhamController {
    @Autowired
    private SanPhamService sanPhamService;

    @RequirePermission("XEM_SAN_PHAM")
    @GetMapping("/hien-thi-all")
    public List<SanPhamResponse> getSanPham() {
        return sanPhamService.getAllSanPham();
    }

    @RequirePermission("XEM_SAN_PHAM")
    @GetMapping("/mon-le")
    public ResponseEntity<?> getAllMonLe() {
        return ResponseEntity.ok(sanPhamService.getAllMonLe());
    }


    @RequirePermission("THEM_SAN_PHAM")
    @PostMapping(value = "/add-san-pham", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addSanPham(
            @Valid @ModelAttribute SanPhamListRequest wrapper,
            BindingResult result,
            @RequestParam("file") MultipartFile file) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldError().getDefaultMessage());
        }

        sanPhamService.addSanPham(wrapper.getRequests(), file);
        return ResponseEntity.ok("Thêm danh sách thành công");
    }

    @RequirePermission("XEM_SAN_PHAM")
    @GetMapping("/detail/{id}")
    public SanPhamResponse getSanPhamById(@PathVariable Integer id) {
        return sanPhamService.findById(id);
    }

    @RequirePermission(("XEM_SAN_PHAM"))
    @GetMapping("/by-danh-muc/{idDanhMuc}")
    public List<SanPhamResponse> getByDanhMuc(@PathVariable Integer idDanhMuc) {
        return sanPhamService.findSanPhamByDanhMuc(idDanhMuc);
    }

    @RequirePermission("SUA_SAN_PHAM")
    @PutMapping(value = "/update/{id}")
    public ResponseEntity<?> updateSanPham(
            @PathVariable Integer id,
            @Valid @ModelAttribute SanPhamRequest request,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(result.getFieldError().getDefaultMessage());
        }

        sanPhamService.updateSanPham(id, request, file);

        return ResponseEntity.ok("Cập nhật thành công");
    }

    @RequirePermission("XEM_SAN_PHAM")
    @GetMapping("/search")
    public List<SanPhamResponse> searchSanPham(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer idDanhMuc,
            @RequestParam(required = false) Boolean trangThaiXoa,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String sortGia
    ) {

        SanPhamSearchRequest request = new SanPhamSearchRequest();

        request.setKeyword(keyword);
        request.setIdDanhMuc(idDanhMuc);
        request.setTrangThaiXoa(trangThaiXoa);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setSortGia(sortGia);

        return sanPhamService.searchSanPham(request);
    }

    @RequirePermission("SUA_SAN_PHAM")
    @PutMapping("/chuyen-trang-thai/{id}")
    public ResponseEntity<?> chuyenTrangThai(@PathVariable Integer id) {
        sanPhamService.chuyenTrangThai(id);
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }

    @GetMapping("/set-lau")
    public List<SanPhamResponse> getSetLau() {
        return sanPhamService.getSetLau();
    }

    @GetMapping("/hien-thi-sp-dgg")
    public List<SanPhamResponse> getSanPhamDgg() {
        return sanPhamService.getAllSanPhamDgg();
    }

    @GetMapping("/san-pham-dot-giam-gia")
    public ResponseEntity<?> getSanPhamDotGiamGia(@RequestParam Boolean laSet) {
        return ResponseEntity.ok(sanPhamService.getSanPhamTheoLoai(laSet));
    }


}
