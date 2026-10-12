package com.fourspicehotpot.backend.modules.dinhluong.controller;

import com.fourspicehotpot.backend.modules.dinhluong.service.DonViService;
import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import com.fourspicehotpot.backend.modules.dinhluong.dto.request.DonViRequest;
import com.fourspicehotpot.backend.modules.dinhluong.dto.response.DonViResponse;
import com.fourspicehotpot.backend.modules.dinhluong.repository.DonViRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/don-vi")
@CrossOrigin(originPatterns = "http://localhost:*")
public class DonViController {

    @Autowired
    private DonViRepository donViRepository;

    @Autowired
    private DonViService donViService;

    @RequirePermission("XEM_THUOC_TINH")
    @GetMapping("/hien-thi")
    public List<DonViResponse> getAllDonVi() {
        return donViService.getAllDonVi();
    }

    @RequirePermission("THEM_THUOC_TINH")
    @PostMapping("/add")
    public ResponseEntity<?> createDonVi(@RequestBody DonViRequest request) {
        donViService.addDonVi(request);
        return ResponseEntity.ok("Tạo đơn vị thành công");
    }

    @RequirePermission("XEM_THUOC_TINH")
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getDonViById(@PathVariable Long id) {
        return ResponseEntity.ok(donViService.findById(id));
    }

    @RequirePermission("SUA_THUOC_TINH")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDonVi(@PathVariable Long id, @RequestBody DonViRequest request) {
        donViService.updateDonViTongHop(id, request);
        return ResponseEntity.ok("Cập nhật thành công");
    }

    @RequirePermission("XEM_THUOC_TINH")
    @GetMapping("/search")
    public List<DonViResponse> searchDonVi(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean trangThaiXoa
    ) {
        return donViService.searchDonVi(keyword, trangThaiXoa);
    }

    @RequirePermission("SUA_THUOC_TINH")
    @PutMapping("/chuyen-trang-thai/{id}")
    public ResponseEntity<?> chuyenTrangThai(@PathVariable Long id) {

        donViService.chuyenTrangThai(id);

        return ResponseEntity.ok("Đổi trạng thái thành công");
    }
}
