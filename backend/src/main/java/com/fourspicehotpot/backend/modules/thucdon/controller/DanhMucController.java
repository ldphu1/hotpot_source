package com.fourspicehotpot.backend.modules.thucdon.controller;

import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import com.fourspicehotpot.backend.modules.thucdon.dto.request.DanhMucRequest;
import com.fourspicehotpot.backend.modules.thucdon.dto.response.ApiResponse;
import com.fourspicehotpot.backend.modules.thucdon.dto.response.DanhMucResponse;
import com.fourspicehotpot.backend.modules.thucdon.service.DanhMucService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@Controller
@RestController
@RequestMapping("/api/admin/danh-muc")
@CrossOrigin(originPatterns = "http://localhost:*")
public class DanhMucController {

    @Autowired
    private DanhMucService danhMucService;


    @RequirePermission("XEM_DANH_MUC")
    @GetMapping("/hien-thi")
    public List<DanhMucResponse> hienThi(Model model) {
        return danhMucService.findAllDanhMucHoatDong();
    }

    @RequirePermission("XEM_SAN_PHAM")
    @GetMapping("/mon-le")
    public List<DanhMucResponse> findAllDanhMucMonLe() {
        return danhMucService.findAllDanhMucMonLe();
    }

    @RequirePermission("THEM_DANH_MUC")
    @PostMapping("/add")
    public ResponseEntity<?> addDanhMuc(
            @Valid @RequestBody DanhMucRequest request,
            BindingResult result
    ) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    result.getFieldErrors().get(0).getDefaultMessage()
            );
        }

        try {

            danhMucService.addDanhMuc(request);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Thêm danh mục thành công", null)
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequirePermission("XEM_DANH_MUC")
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findDanhMucById(@PathVariable Integer id) {
        return ResponseEntity.ok(danhMucService.getDanhMucById(id));
    }

    @RequirePermission("SUA_DANH_MUC")
    @PutMapping("/update")
    public ResponseEntity<?> updateDanhMuc(
            @Valid @RequestBody DanhMucRequest request,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    result.getFieldErrors().get(0).getDefaultMessage()
            );
        }
        try {
            danhMucService.updateDanhMuc(request);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Sửa danh mục thành công", null)
            );
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Tên danh mục đã tồn tại");
        }
    }

    @RequirePermission("XOA_DANH_MUC")
    @PutMapping("/delete/{id}")
    public ResponseEntity<?> deleteDanhMuc(@PathVariable Integer id) {
        danhMucService.deleteDanhMuc(id);
        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật trạng thái thành công", null));
    }

    @RequirePermission("XOA_DANH_MUC")
    @PutMapping("/restore/{id}")
    public ResponseEntity<?> restoreDanhMuc(@PathVariable Integer id) {
        danhMucService.restoreDanhMuc(id);
        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật trạng thái thành công", null));
    }

    @RequirePermission("XEM_DANH_MUC")
    @GetMapping("/search")
    public ResponseEntity<?> filterDanhMuc(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer idDanhMuc,
                                           @RequestParam(required = false) Boolean trangThaiXoa) {
        return ResponseEntity.ok(
                danhMucService.filterDanhMuc(keyword, idDanhMuc, trangThaiXoa)
        );
    }


}
