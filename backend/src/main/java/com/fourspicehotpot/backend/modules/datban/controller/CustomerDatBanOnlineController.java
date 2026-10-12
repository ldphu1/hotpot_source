package com.fourspicehotpot.backend.modules.datban.controller;

import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.banan.service.BanAnService;
import com.fourspicehotpot.backend.modules.datban.dto.request.CustomerDatBanRequest;
import com.fourspicehotpot.backend.modules.datban.dto.response.DatBanResponse;
import com.fourspicehotpot.backend.modules.datban.service.CustomerDatBanService;
import com.fourspicehotpot.backend.modules.datban.service.DatBanService;
import com.fourspicehotpot.backend.modules.giohang.service.GioHangService;
import com.fourspicehotpot.backend.modules.thamsohethong.service.ThamSoHeThongService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/public")
public class CustomerDatBanOnlineController {
    @Autowired
    private CustomerDatBanService customerDatBanService;

    @Autowired
    private BanAnService quanLyBanService;
    @Autowired
    private ThamSoHeThongService thamSoHeThongService;
    @Autowired
    private DatBanService datBanService;
    @Autowired
    private GioHangService gioHangService;

    @PostMapping("/dat-ban-online")
    public ResponseEntity<?> saveDatBanOnline(
            @RequestBody @Valid CustomerDatBanRequest request,
            BindingResult result,
            HttpServletRequest httpRequest) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        String vnpayUrl = customerDatBanService.datBanOnline(request, httpRequest);

        return ResponseEntity.ok(vnpayUrl);
    }


    @GetMapping("/ban/hien-thi")
    public List<BanAn> hienThi() {
        return quanLyBanService.findAll();
    }


    @GetMapping("/tham-so/hien-thi")
    public ResponseEntity<?> getAllThamSo() {
        return ResponseEntity.ok(thamSoHeThongService.getAllThamSo());
    }

    @GetMapping("/dat-ban/hien-thi")
    public List<DatBanResponse> findAll() {
        return datBanService.findAll();
    }


}
