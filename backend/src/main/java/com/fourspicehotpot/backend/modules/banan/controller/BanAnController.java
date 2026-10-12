package com.fourspicehotpot.backend.modules.banan.controller;

import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import com.fourspicehotpot.backend.modules.banan.dto.request.BanAnSerachRequest;
import com.fourspicehotpot.backend.modules.banan.dto.response.BanAnResponse;
import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.banan.service.BanAnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(originPatterns = "http://localhost:*")
@RequestMapping("/api/admin/quan-ly-ban")
public class BanAnController {

    @Autowired
    private BanAnService banAnService;

    @RequirePermission("XEM_BAN")
    @GetMapping("/hien-thi")
    public ResponseEntity<List<BanAn>> hienThi(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay
    ) {
        LocalDate ngayCanLay = (ngay != null) ? ngay : LocalDate.now();
        return ResponseEntity.ok(banAnService.getDanhSachBanTheoNgay(ngayCanLay));
    }


    @RequirePermission("THEM_BAN")
    @PostMapping("/themBanAn")
    public ResponseEntity<?> save(@RequestBody BanAn banAn) {
        try {
            BanAn result = banAnService.save(banAn);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequirePermission("XEM_BAN")
    @GetMapping("/chi-tiet/{id}")
    public BanAn chiTiet(@PathVariable Integer id) {
        return banAnService.findById(id);
    }

    @RequirePermission("XOA_BAN")
    @GetMapping("/xoa/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Integer id) {
        try {
            banAnService.deleteBanAn(id);
            return ResponseEntity.ok("Xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequirePermission("SUA_BAN")
    @PutMapping("/doi-trang-thai/{id}")
    public ResponseEntity<?> doiTrangThai(
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> body) {

        Integer trangThai = body.get("trangThai");
        BanAn ban = banAnService.findById(id);
        ban.setTrangThai(trangThai);
        banAnService.save(ban);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/tim-kiem")
    public List<BanAn> searchBan(@RequestParam(required = false) String keyword) {
        return banAnService.searchBanAn(keyword);
    }

    @GetMapping("/danh-sach")
    public List<BanAnResponse> getDanhSachBan() {
        return banAnService.getDanhSachBan();
    }

    @RequirePermission("XEM_BAN")
    @GetMapping("/filter")
    public List<BanAnResponse> filterBanAn(
            @RequestParam(required = false) String tenKhuVuc,
            @RequestParam(required = false) Integer soChoToiDa,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam LocalDateTime thoiGianDat

    ) {
        BanAnSerachRequest request = new BanAnSerachRequest();
        request.setTang(tenKhuVuc);
        request.setSoCho(soChoToiDa);
        request.setTrangThai(trangThai);

        return banAnService.filterBanAn(request, thoiGianDat);


    }

}
