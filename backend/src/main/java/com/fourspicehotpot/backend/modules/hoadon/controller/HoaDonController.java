package com.fourspicehotpot.backend.modules.hoadon.controller;

import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.hoadon.service.HoaDonChiTietService;
import com.fourspicehotpot.backend.modules.hoadon.service.HoaDonService;
import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import com.fourspicehotpot.backend.modules.checkin.dto.request.CheckInRequest;
import com.fourspicehotpot.backend.modules.hoadon.dto.request.HoaDonRequest;
import com.fourspicehotpot.backend.modules.hoadon.dto.request.HoaDonSearchRequest;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonDetailResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin(originPatterns = "http://localhost:*")
@RequestMapping("/api/admin/hoa-don")
public class HoaDonController {
    @Autowired
    HoaDonService hoaDonService;

    @Autowired
    private HoaDonChiTietService hoaDonChiTietService;

    @GetMapping("/hien-thi")
    public List<HoaDonResponse> hienThi() {
        return hoaDonService.hienThi();
    }

    //    public HoaDon checkin(@RequestBody CheckInRequest request) {
//        return hoaDonService.checkin(request);
//
//    }
//
//    // lấy hóa đơn theo bàn
//    @GetMapping("/ban/{idBan}")
//    public HoaDon getHoaDonByBan(@PathVariable Long idBan) {
//        return hoaDonService.getHoaDonDangMoByBan(idBan);
//    }
//
//    // thanh toán
//    @PostMapping("/thanh-toan/{idHoaDon}")
//    public HoaDon thanhToan(@PathVariable Long idHoaDon) {
//        return hoaDonService.thanhToan(idHoaDon);
//    }
    // Không được sửa
    @RequirePermission("XEM_HOA_DON")
    @GetMapping("/detail/{id}")
    public ResponseEntity<HoaDonDetailResponse> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(hoaDonChiTietService.getDetail(id));
    }

    @RequirePermission(("SUA_HOA_DON"))
    @PutMapping("/xac-nhan/{id}")
    public void xacNhan(@PathVariable Long id) {
        hoaDonService.chuyenTrangThai(id);
    }

    // checkin
    @PostMapping("/checkin")
    public HoaDon checkin(@RequestBody CheckInRequest request) {
        return hoaDonService.checkin(request);
    }

    // lấy hóa đơn theo bàn
    @GetMapping("/ban/{idBan}")
    public HoaDon getHoaDonByBan(@PathVariable Long idBan) {
        return hoaDonService.getHoaDonDangMoByBan(idBan);
    }

    // thanh toán
    @PostMapping("/thanh-toan/{idHoaDon}")
    public HoaDon thanhToan(@PathVariable Long idHoaDon) {
        return hoaDonService.thanhToan(idHoaDon);
    }


    // Không được sửa
    @GetMapping("/search")
    public List<HoaDonResponse> searchHoaDon(
            @RequestParam(required = false) String soDienThoai,
            @RequestParam(required = false) String tenKhachHang,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String loaiHoaDon,
            @RequestParam(required = false) String tenKhuVuc,
            @RequestParam(required = false) LocalDate ngayBatDau,
            @RequestParam(required = false) LocalDate ngayKetThuc
    ) {
        HoaDonSearchRequest request = new HoaDonSearchRequest();
        request.setSoDienThoai(soDienThoai);
        request.setTenKhachHang(tenKhachHang);
        request.setTrangThai(trangThai);
        request.setLoaiHoaDon(loaiHoaDon);
        request.setTenKhuVuc(tenKhuVuc);
        request.setNgayBatDau(ngayBatDau);
        request.setNgayKetThuc(ngayKetThuc);
        return hoaDonService.search(request);
    }

    @PutMapping("/huy/{id}")
    public ResponseEntity<?> updateHoaDon(@PathVariable Long id, @RequestBody HoaDonRequest request) {
        hoaDonService.huyHoaDon(id, request);
        return ResponseEntity.ok("Hủy thành công");

    }

    @PutMapping("/hoan-thanh/{id}")
    public ResponseEntity<?> hoanThanhHoaDon(@PathVariable Long id) {
        hoaDonService.hoanThanhHoaDon(id);
        return ResponseEntity.ok("Hoàn thành hóa đơn");
    }

    @GetMapping("/preview-huy/{id}")
    public ResponseEntity<?> preview(@PathVariable Long id) {
        return ResponseEntity.ok(hoaDonService.previewHuyHoaDon(id));
    }

    @GetMapping("/export-pdf/{id}")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        byte[] pdf = hoaDonService.inHoaDon(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hoa-don.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);

    }
}
