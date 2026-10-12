package com.fourspicehotpot.backend.modules.checkin.controller;

import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonChiTietResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.LichSuHoaDonResponse;
import com.fourspicehotpot.backend.modules.datban.dto.response.DatBanResponse;
import com.fourspicehotpot.backend.modules.phieugiamgia.service.PhieuGiamGiaService;
import com.fourspicehotpot.backend.modules.checkin.dto.request.*;
import com.fourspicehotpot.backend.modules.checkin.dto.response.BanAnTrongResponse;
import com.fourspicehotpot.backend.modules.checkin.dto.response.CheckInDetailResponse;
import com.fourspicehotpot.backend.modules.checkin.dto.response.CheckInResponse;
import com.fourspicehotpot.backend.modules.checkin.dto.response.HoaDonDangDungTheoBanResponse;
import com.fourspicehotpot.backend.modules.checkin.service.CheckInService;
import com.fourspicehotpot.backend.modules.phieugiamgia.entity.PhieuGiamGia;
import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import com.fourspicehotpot.backend.modules.hoadon.service.HoaDonChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(originPatterns = "http://localhost:*")
@RequestMapping("/api/admin/check-in")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private PhieuGiamGiaService phieuGiamGiaService;

    @Autowired
    private HoaDonChiTietService hoaDonChiTietService;


    // ================== DANH SÁCH BÀN ==================
    @GetMapping("/hien-thi")
    public ResponseEntity<List<CheckInResponse>> getBanCheckIn() {
        return ResponseEntity.ok(checkInService.getDanhSachBanCheckIn());
    }

    // ================== CHI TIẾT CHECK-IN THEO BÀN ==================
    @GetMapping("/chi-tiet/{idBanAn}")
    public ResponseEntity<CheckInDetailResponse> getChiTietCheckIn(@PathVariable Integer idBanAn) {
        return ResponseEntity.ok(checkInService.getChiTietCheckIn(idBanAn));
    }

    // ================== XÁC NHẬN CHECK-IN ==================
    @PostMapping("/xac-nhan")
    public ResponseEntity<CheckInResponse> xacNhanCheckIn(@RequestBody CheckInRequest request) {
        return ResponseEntity.ok(checkInService.xacNhanCheckIn(request));
    }

    // ================== THỰC ĐƠN ==================
    @GetMapping("/thuc-don")
    public ResponseEntity<List<SanPhamResponse>> getAllThucDon() {
        return ResponseEntity.ok(checkInService.getAllThucDon());
    }

    @GetMapping("/thuc-don/mon-le")
    public ResponseEntity<List<SanPhamResponse>> getAllMonLe() {
        return ResponseEntity.ok(checkInService.getAllMonLe());
    }

    @GetMapping("/thuc-don/set-lau")
    public ResponseEntity<List<SanPhamResponse>> getAllSetLau() {
        return ResponseEntity.ok(checkInService.getAllSetLau());
    }

    @GetMapping("/thuc-don/search-mon-le")
    public ResponseEntity<List<SanPhamResponse>> searchMonLe(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long idDanhMuc,
            @RequestParam(required = false) BigDecimal giaMin,
            @RequestParam(required = false) BigDecimal giaMax
    ) {
        return ResponseEntity.ok(
                checkInService.searchMonLe(keyword, idDanhMuc, giaMin, giaMax)
        );
    }

    @GetMapping("/thuc-don/search-set-lau")
    public ResponseEntity<List<SanPhamResponse>> searchSetLau(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long idNuocLau
    ) {
        return ResponseEntity.ok(
                checkInService.searchSetLau(keyword, idNuocLau)
        );
    }

    // ================== THÊM MÓN VÀO HÓA ĐƠN ==================
    @PostMapping("/them-mon")
    public ResponseEntity<HoaDonChiTietResponse> themMonVaoHoaDon(@RequestBody ThemMonCheckInRequest request) {
        return ResponseEntity.ok(
                checkInService.themMonVaoHoaDon(
                        request.getIdHoaDon(),
                        request.getIdSanPham(),
                        request.getSoLuong(),
                        request.getTenBan(),
                        request.getIdBanAn()
                )
        );
    }

    // ================== LẤY MÓN ĐÃ CHỌN THEO HÓA ĐƠN ==================
    @GetMapping("/hoa-don/{idHoaDon}/mon-da-chon")
    public ResponseEntity<List<HoaDonChiTietResponse>> getMonDaChon(@PathVariable Long idHoaDon) {
        return ResponseEntity.ok(checkInService.getMonDaChonTheoHoaDon(idHoaDon));
    }

    // ================== LỊCH SỬ HÓA ĐƠN ==================
    @GetMapping("/lich-su-hoa-don/{idHoaDon}")
    public ResponseEntity<List<LichSuHoaDonResponse>> getLichSuHoaDon(@PathVariable Long idHoaDon) {
        return ResponseEntity.ok(checkInService.getLichSuHoaDon(idHoaDon));
    }

    // ================== ĐÃ LÊN 1 MÓN ==================
    @PutMapping("/mon/{idHoaDonChiTiet}/da-len")
    public ResponseEntity<HoaDonChiTietResponse> daLenMon(
            @PathVariable Long idHoaDonChiTiet,
            @RequestParam(required = false) String tenBan
    ) {
        return ResponseEntity.ok(checkInService.daLenMon(idHoaDonChiTiet, tenBan ));
    }

    // ================== ĐÃ LÊN TẤT CẢ MÓN ==================
    @PutMapping("/hoa-don/{idHoaDon}/da-len-tat-ca")
    public ResponseEntity<List<HoaDonChiTietResponse>> daLenTatCa(
            @PathVariable Long idHoaDon,
            @RequestParam(required = false) String tenBan
    ) {
        return ResponseEntity.ok(checkInService.daLenTatCa(idHoaDon, tenBan));
    }

    // ================== XÓA 1 MÓN KHỎI HÓA ĐƠN ==================
    @DeleteMapping("/mon/{idHoaDonChiTiet}")
    public ResponseEntity<String> xoaMonKhoiHoaDon(
            @PathVariable Long idHoaDonChiTiet,
            @RequestParam(required = false) String tenBan
    ) {
        checkInService.xoaMonKhoiHoaDon(idHoaDonChiTiet, tenBan);
        return ResponseEntity.ok("Xóa món khỏi hóa đơn thành công");
    }

    // ================== VOUCHER MAPPER ==================
    private Map<String, Object> toVoucherResponse(PhieuGiamGia p) {
        if (p == null) return null;

        Map<String, Object> map = new HashMap<>();
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

    // ================== DANH SÁCH PHIẾU HỢP LỆ CHUNG ==================
    @GetMapping("/hoa-don/{idHoaDon}/phieu-giam-gia-hop-le")
    public ResponseEntity<List<Map<String, Object>>> getDanhSachPhieuGiamGiaHopLe(@PathVariable Long idHoaDon) {
        List<PhieuGiamGia> ds = checkInService.getDanhSachPhieuGiamGiaHopLe(idHoaDon);
        return ResponseEntity.ok(
                ds.stream().map(this::toVoucherResponse).toList()
        );
    }

    // ================== DANH SÁCH PHIẾU THEO KHÁCH HÀNG ==================
    @GetMapping("/hoa-don/{idHoaDon}/phieu-giam-gia-khach-hang")
    public ResponseEntity<List<Map<String, Object>>> getDanhSachPhieuGiamGiaTheoKhachHang(@PathVariable Long idHoaDon) {
        List<PhieuGiamGia> ds = checkInService.getDanhSachPhieuGiamGiaTheoKhachHang(idHoaDon);
        return ResponseEntity.ok(
                ds.stream().map(this::toVoucherResponse).toList()
        );
    }

    // ================== PHIẾU TỐT NHẤT ==================
    @GetMapping("/hoa-don/{idHoaDon}/phieu-giam-gia-tot-nhat")
    public ResponseEntity<Map<String, Object>> getPhieuGiamGiaTotNhat(@PathVariable Long idHoaDon) {
        PhieuGiamGia p = checkInService.getPhieuGiamGiaTotNhat(idHoaDon);
        return ResponseEntity.ok(toVoucherResponse(p));
    }

    // ================== ÁP DỤNG MÃ GIẢM GIÁ ==================
    @PostMapping("/hoa-don/{idHoaDon}/ap-dung-ma-giam-gia")
    public ResponseEntity<HoaDonResponse> apDungMaGiamGia(
            @PathVariable Long idHoaDon,
            @RequestBody ApDungMaGiamGiaRequest request
    ) {
        return ResponseEntity.ok(checkInService.apDungMaGiamGia(idHoaDon, request.getMaGiamGia()));
    }

    // ================== ĐỔI BÀN ==================
    @PutMapping("/doi-ban")
    public ResponseEntity<CheckInResponse> doiBan(@RequestBody DoiBanRequest request) {
        return ResponseEntity.ok(checkInService.doiBan(request));
    }

    // ================== GHÉP BÀN ==================
    @PutMapping("/ghep-ban")
    public ResponseEntity<CheckInResponse> ghepBan(@RequestBody GhepBanRequest request) {
        return ResponseEntity.ok(checkInService.ghepBan(request));
    }

    // ================== HIỂN THỊ BÀN TRỐNG ==================
    @GetMapping("/ban-trong")
    public ResponseEntity<List<BanAnTrongResponse>> getBanTrong() {
        return ResponseEntity.ok(checkInService.getBanTrong());
    }

    // ================== TÁCH BÀN ==================
    @PutMapping("/tach-ban")
    public ResponseEntity<CheckInResponse> tachBan(@RequestBody TachBanRequest request) {
        return ResponseEntity.ok(checkInService.tachBan(request));
    }

    // ================== GHÉP 2 BÀN ĐANG DÙNG ==================
    @PutMapping("/ghep-hai-ban-dang-dung")
    public ResponseEntity<CheckInResponse> ghepHaiBanDangDung(
            @RequestBody GhepBanDangDungRequest request
    ) {
        return ResponseEntity.ok(checkInService.ghepHaiBanDangDung(request));
    }

    // ================== TÌM KIẾM ĐẶT BÀN ==================
    @GetMapping("/search-dat-ban")
    public ResponseEntity<List<DatBanResponse>> searchDatBan(
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(checkInService.searchDatBan(keyword));
    }

    // chi tiết hóa đơn gộp đang sử dụng
    @GetMapping("/ban/{idBanAn}/hoa-don-dang-dung")
    public ResponseEntity<List<HoaDonDangDungTheoBanResponse>> getHoaDonDangDungTheoBan(
            @PathVariable Integer idBanAn
    ) {
        return ResponseEntity.ok(checkInService.getHoaDonDangDungTheoBan(idBanAn));
    }

    @GetMapping("/chi-tiet/{idBanAn}/hoa-don/{idHoaDon}")
    public ResponseEntity<CheckInDetailResponse> getChiTietCheckInTheoHoaDon(
            @PathVariable Integer idBanAn,
            @PathVariable Long idHoaDon
    ) {
        return ResponseEntity.ok(checkInService.getChiTietCheckInTheoHoaDon(idBanAn, idHoaDon));
    }

    // tạo hóa đơn trên bàn đang dùng
    @PostMapping("/tao-hoa-don-moi-tren-ban")
    public ResponseEntity<CheckInResponse> taoHoaDonMoiTrenBan(
            @RequestBody TaoHoaDonMoiTrenBanRequest request
    ) {
        return ResponseEntity.ok(checkInService.taoHoaDonMoiTrenBan(request));
    }

    // ================== XUẤT PDF GỌI MÓN ==================
    @GetMapping("/hoa-don/{idHoaDon}/export-pdf-goi-mon")
    public ResponseEntity<byte[]> exportPdfGoiMon(
            @PathVariable Long idHoaDon,
            @RequestParam(required = false) List<Long> chiTietIds
    ) {
        byte[] pdfBytes = ((com.fourspicehotpot.backend.modules.checkin.service.serviceImpl.CheckInServiceImpl) checkInService).exportPdfGoiMon(idHoaDon, chiTietIds);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=goi_mon_" + idHoaDon + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/xac-thuc-ma")
    public ResponseEntity<Boolean> xacThucMaDatBan(
            @RequestParam Long idDatBan,
            @RequestParam String maDatBan
    ) {
        return ResponseEntity.ok(checkInService.xacThucMaDatBan(idDatBan, maDatBan));
    }
}