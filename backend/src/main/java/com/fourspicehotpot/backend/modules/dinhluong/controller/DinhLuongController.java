package com.fourspicehotpot.backend.modules.dinhluong.controller;

import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import com.fourspicehotpot.backend.modules.dinhluong.dto.response.DinhLuongResponse;
import com.fourspicehotpot.backend.modules.dinhluong.repository.DinhLuongRepository;
import com.fourspicehotpot.backend.modules.dinhluong.service.DinhLuongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dinh-luong")
@CrossOrigin(originPatterns = "http://localhost:*")
public class DinhLuongController {

    @Autowired
    private DinhLuongRepository dinhLuongRepository;

    @Autowired
    private DinhLuongService dinhLuongService;

    @RequirePermission("XEM_THUOC_TINH")
    @GetMapping("/hien-thi")
    public List<DinhLuongResponse> hienThi(Model model) {
        return dinhLuongService.getAllDinhLuong();
    }

    @RequirePermission("XEM_THUOC_TINH")
    @GetMapping("/by-danh-muc/{idDanhMuc}")
    public ResponseEntity<?> getByDanhMuc(@PathVariable Integer idDanhMuc) {
        return ResponseEntity.ok(dinhLuongService.getDinhLuongByDanhMuc(idDanhMuc));
    }
}
