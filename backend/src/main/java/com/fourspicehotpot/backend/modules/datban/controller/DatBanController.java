package com.fourspicehotpot.backend.modules.datban.controller;

import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import com.fourspicehotpot.backend.modules.banan.dto.response.BanAnResponse;
import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.banan.service.BanAnService;
import com.fourspicehotpot.backend.modules.checkin.service.CheckInService;
import com.fourspicehotpot.backend.modules.datban.dto.request.DatBanRequest;
import com.fourspicehotpot.backend.modules.datban.dto.request.DatBanSearchRequest;
import com.fourspicehotpot.backend.modules.checkin.dto.request.SearchKhachChoCheckInRequest;
import com.fourspicehotpot.backend.modules.datban.dto.request.XepBanRequest;
import com.fourspicehotpot.backend.modules.datban.dto.response.DatBanResponse;
import com.fourspicehotpot.backend.modules.datban.service.DatBanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(originPatterns = "http://localhost:*")
@RequestMapping("/api/admin/dat-ban")
public class DatBanController {


    private final DatBanService datBanService;
    private final BanAnService banAnService;
    private final CheckInService checkInService;

    public DatBanController(
            DatBanService datBanService,
            BanAnService banAnService,
            CheckInService checkInService) {

        this.datBanService = datBanService;
        this.banAnService = banAnService;
        this.checkInService = checkInService;
    }

    // Lãnh Minh Hiếu
    @RequirePermission("XEM_BAN")
    @GetMapping("/chon-ban/hien-thi")
    public ResponseEntity<List<BanAn>> hienThiBoSung(

    ) {
        return ResponseEntity.ok(banAnService.findAll());
    }

    @GetMapping("/ban-an/cho-dat")
    public List<BanAnResponse> getBanChoDat(@RequestParam LocalDateTime thoiGianDat) {
        return banAnService.getDanhSachBanChoDat(thoiGianDat);
    }

    @RequirePermission("XEM_BAN")
    @GetMapping("/hien-thi")
    public List<DatBanResponse> findAll() {
        return datBanService.findAll();
    }

    @RequirePermission("THEM_BAN")
    @PostMapping("/add")
    public ResponseEntity<?> datBanTaiQuay(@RequestBody DatBanRequest datBanRequest) {
        return ResponseEntity.ok(datBanService.datBanTaiQuay(datBanRequest));
    }

    @RequirePermission("SUA_BAN")
    @PostMapping("/xep-ban")
    public ResponseEntity<?> xepBan(@RequestBody XepBanRequest request) {
        return ResponseEntity.ok(datBanService.xepBanTaiQuay(request));
    }

    @RequirePermission("SUA_BAN")
    @PostMapping("/xep-lai-ban")
    public ResponseEntity<?> xepLaiBan(@RequestBody XepBanRequest request) {
        return ResponseEntity.ok(datBanService.xepLaiBan(request));
    }

    // @RequirePermission(("XEM_BAN"))
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.ok(datBanService.findById(id));
    }

    @PutMapping("/xac-nhan/{id}")
    public ResponseEntity<?> xacNhanDatBan(@PathVariable Long id) {
        return ResponseEntity.ok(datBanService.xacNhanDatBan(id));
    }

    @PutMapping("/xac-nhan-coc/{id}")
    public ResponseEntity<?> xacNhanCocDatBan(
            @PathVariable Integer id,
            @RequestBody DatBanRequest request) {
        request.setId(id);
        return ResponseEntity.ok(
                datBanService.xacNhanCocVaXacNhan(request)
        );
    }


    @GetMapping("/hien-thi-theo-ngay-1")
    public ResponseEntity<List<DatBanResponse>> findByNgay(
            @ModelAttribute SearchKhachChoCheckInRequest request
    ) {
        return ResponseEntity.ok(checkInService.findByNgay(request));
    }

    @GetMapping("/search")
    public List<DatBanResponse> searchDatBan(@ModelAttribute DatBanSearchRequest request) {
        return datBanService.searchDatBan(request);
    }
}
