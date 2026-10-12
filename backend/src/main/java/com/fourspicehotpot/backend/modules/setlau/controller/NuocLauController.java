package com.fourspicehotpot.backend.modules.setlau.controller;

import com.fourspicehotpot.backend.modules.setlau.service.NuocLauService;
import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import com.fourspicehotpot.backend.modules.setlau.dto.request.NuocLauRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.response.NuocLauResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/nuoc-lau")
@CrossOrigin(originPatterns = "http://localhost:*")
public class NuocLauController {

    @Autowired
    private NuocLauService nuocLauService;

    @RequirePermission("XEM_NUOC_LAU")
    @GetMapping("/hien-thi")
    public List<NuocLauResponse> hienThi() {

        return nuocLauService.getAllNuocLau();
    }

    @RequirePermission("THEM_NUOC_LAU")
    @PostMapping("/add")
    public ResponseEntity<?> addNuocLau(
            @Valid @ModelAttribute NuocLauRequest request,
            @RequestParam("file") MultipartFile file
    ) {
        System.out.println("Ten nuoc lau: " + request.getTenNuocLau());
        System.out.println("Gia phu thu: " + request.getGiaPhuThu());
        System.out.println("Mo ta: " + request.getMoTa());
        System.out.println("File: " + file.getOriginalFilename());
        nuocLauService.addNuocLau(request, file);
        return ResponseEntity.ok("OK");
    }

    @RequirePermission("XEM_NUOC_LAU")
    @GetMapping("/detail/{id}")
    public ResponseEntity<NuocLauResponse> detailNuocLau(@PathVariable Long id) {
        return ResponseEntity.ok(nuocLauService.getNuocLauById(id));
    }

    @RequirePermission("SUA_NUOC_LAU")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateNuocLau(
            @PathVariable Long id,
            @Valid @ModelAttribute NuocLauRequest request,
            @RequestParam(required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(nuocLauService.updateNuocLau(id, request, file));
    }

    @RequirePermission("XEM_NUOC_LAU")
    @GetMapping("/search")
    public List<NuocLauResponse> searchNuocLau(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(required = false) String sortGia
    ) {

        NuocLauRequest req = new NuocLauRequest();
        req.setKeyword(keyword);
        req.setTrangThaiXoa(trangThai);
        req.setSortGia(sortGia);

        return nuocLauService.searchNuocLau(req);
    }

    @RequirePermission("SUA_NUOC_LAU")
    @PutMapping("/chuyen-trang-thai/{id}")
    public ResponseEntity<?> chuyenTrangThai(@PathVariable Integer id) {
        nuocLauService.chuyenTrangThai(id);
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }
}
