package com.fourspicehotpot.backend.modules.app.controller;

import com.fourspicehotpot.backend.modules.checkin.dto.response.HoaDonDangDungTheoBanResponse;
import com.fourspicehotpot.backend.modules.checkin.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(originPatterns = "http://localhost:*")
@RequestMapping("/api/public/app/check-in")
public class PublicCheckInController {

    @Autowired
    private CheckInService checkInService;

    // Lấy chi tiết hóa đơn gộp đang sử dụng
    @GetMapping("/ban/{idBanAn}/hoa-don-dang-dung")
    public ResponseEntity<List<HoaDonDangDungTheoBanResponse>> getHoaDonDangDungTheoBan(
            @PathVariable Integer idBanAn
    ) {
        return ResponseEntity.ok(checkInService.getHoaDonDangDungTheoBan(idBanAn));
    }

    // Lấy chi tiết hóa đơn
    @GetMapping("/chi-tiet/{idBanAn}/hoa-don/{idHoaDon}")
    public ResponseEntity<com.fourspicehotpot.backend.modules.checkin.dto.response.CheckInDetailResponse> getChiTietCheckInTheoHoaDon(
            @PathVariable Integer idBanAn,
            @PathVariable Long idHoaDon
    ) {
        return ResponseEntity.ok(checkInService.getChiTietCheckInTheoHoaDon(idBanAn, idHoaDon));
    }

    // Lấy danh sách món đã chọn
    @GetMapping("/hoa-don/{idHoaDon}/mon-da-chon")
    public ResponseEntity<List<com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonChiTietResponse>> getMonDaChonTheoHoaDon(
            @PathVariable Long idHoaDon
    ) {
        return ResponseEntity.ok(checkInService.getMonDaChonTheoHoaDon(idHoaDon));
    }

    // Xuất PDF hóa đơn gọi món
    @GetMapping("/hoa-don/{idHoaDon}/export-pdf-goi-mon")
    public ResponseEntity<byte[]> exportPdfGoiMon(
            @PathVariable Long idHoaDon,
            @RequestParam(required = false) List<Long> chiTietIds
    ) {
        byte[] pdfBytes = ((com.fourspicehotpot.backend.modules.checkin.service.serviceImpl.CheckInServiceImpl) checkInService).exportPdfGoiMon(idHoaDon, chiTietIds);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=goi_mon_" + idHoaDon + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    private java.util.Map<String, Object> toVoucherResponse(com.fourspicehotpot.backend.modules.phieugiamgia.entity.PhieuGiamGia p) {
        if (p == null) return null;
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", p.getId());
        map.put("maGiamGia", p.getMaGiamGia());
        map.put("tenGiamGia", p.getTenGiamGia());
        map.put("phuongThucGiamGia", p.getPhuongThucGiamGia());
        map.put("kieuGiamGia", p.getKieuGiamGia());
        map.put("giaTriGiam", p.getGiaTriGiam());
        map.put("giamToiDa", p.getGiamToiDa());
        map.put("dieuKienSuDung", p.getDieuKienSuDung());
        map.put("ngayBatDau", p.getNgayBatDau());
        map.put("ngayKetThuc", p.getNgayKetThuc());
        map.put("soLuong", p.getSoLuong());
        return map;
    }

    @GetMapping("/hoa-don/{idHoaDon}/phieu-giam-gia-hop-le")
    public ResponseEntity<List<java.util.Map<String, Object>>> getDanhSachPhieuGiamGiaHopLe(@PathVariable Long idHoaDon) {
        List<com.fourspicehotpot.backend.modules.phieugiamgia.entity.PhieuGiamGia> ds = checkInService.getDanhSachPhieuGiamGiaHopLe(idHoaDon);
        return ResponseEntity.ok(ds.stream().map(this::toVoucherResponse).toList());
    }

    @GetMapping("/hoa-don/{idHoaDon}/phieu-giam-gia-khach-hang")
    public ResponseEntity<List<java.util.Map<String, Object>>> getDanhSachPhieuGiamGiaTheoKhachHang(@PathVariable Long idHoaDon) {
        List<com.fourspicehotpot.backend.modules.phieugiamgia.entity.PhieuGiamGia> ds = checkInService.getDanhSachPhieuGiamGiaTheoKhachHang(idHoaDon);
        return ResponseEntity.ok(ds.stream().map(this::toVoucherResponse).toList());
    }
}
