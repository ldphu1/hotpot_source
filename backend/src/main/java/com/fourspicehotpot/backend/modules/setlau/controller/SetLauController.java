package com.fourspicehotpot.backend.modules.setlau.controller;

import com.fourspicehotpot.backend.modules.setlau.service.SanPhamService;
import com.fourspicehotpot.backend.modules.setlau.service.SetLauService;
import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import com.fourspicehotpot.backend.modules.setlau.dto.request.SetLauRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/set-lau")
@CrossOrigin(originPatterns = "http://localhost:*")
public class SetLauController {
    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private SetLauService setLauService;

    // Set lẩu
    @RequirePermission("XEM_SAN_PHAM")
    @GetMapping("/hien-thi")
    public List<SanPhamResponse> getSetLau() {

        return sanPhamService.getAllSetLau();
    }

    @RequirePermission("THEM_SAN_PHAM")
    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addSetLau(
            @ModelAttribute SetLauRequest dto,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            setLauService.addSetLau(dto, file);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequirePermission("XOA_SAN_PHAM")
    @GetMapping("/detail/{id}")
    public SanPhamResponse getSetLauById(@PathVariable("id") Integer id) {
        return setLauService.findSetLauById(id);
    }

    @RequirePermission("SUA_SAN_PHAM")
    @PutMapping("/update/{id}")
    public void updateSetLau(
            @PathVariable Integer id,
            @ModelAttribute SetLauRequest dto,
            @RequestParam(required = false) MultipartFile file
    ) {
        try {
            setLauService.updateSetLau(id, dto, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @RequirePermission("XEM_SAN_PHAM")
    @GetMapping("/search")
    public List<SanPhamResponse> searchSetLau(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long idNuocLau,
            @RequestParam(required = false) Boolean trangThaiXoa,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice
    ) {

        return sanPhamService.searchSetLau(
                keyword,
                idNuocLau,
                trangThaiXoa,
                minPrice,
                maxPrice
        );
    }


    @PutMapping("/chuyen-trang-thai/{id}")
    public ResponseEntity<?> chuyenTrangThai(@PathVariable Integer id) {
        sanPhamService.chuyenTrangThai(id);
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }

}
