package com.fourspicehotpot.backend.modules.banan.controller;

import com.fourspicehotpot.backend.modules.banan.entity.KhuVuc;
import com.fourspicehotpot.backend.modules.banan.service.KhuVucService;
import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RestController
@CrossOrigin(originPatterns = "http://localhost:*")
@RequestMapping("/api/admin/khu-vuc")
public class KhuVucController {
    @Autowired
    private KhuVucService khuVucService;

    @RequirePermission("XEM_KHU_VUC")
    @GetMapping("/hien-thi")
    public List<KhuVuc> findByKhuVuc() {
        return khuVucService.findAll();
    }

    @RequirePermission("THEM_KHU_VUC")
    @PostMapping("/themKhuVuc")
    public KhuVuc save(@RequestBody KhuVuc khuVuc) {
        return khuVucService.save(khuVuc);
    }

    @RequirePermission("XOA_KHU_VUC")
    @GetMapping("/xoa/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Integer id) {
        khuVucService.deleteKhuVuc(id);
        return ResponseEntity.ok("Xóa thành công");
    }

    @RequirePermission("XEM_KHU_VUC")
    @GetMapping("/chi-tiet/{id}")
    public ResponseEntity<KhuVuc> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(khuVucService.findById(id));
    }


}
