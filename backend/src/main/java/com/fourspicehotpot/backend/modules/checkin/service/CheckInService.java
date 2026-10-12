package com.fourspicehotpot.backend.modules.checkin.service;

import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.checkin.dto.request.*;
import com.fourspicehotpot.backend.modules.checkin.dto.response.BanAnTrongResponse;
import com.fourspicehotpot.backend.modules.checkin.dto.response.CheckInDetailResponse;
import com.fourspicehotpot.backend.modules.checkin.dto.response.CheckInResponse;
import com.fourspicehotpot.backend.modules.checkin.dto.response.HoaDonDangDungTheoBanResponse;
import com.fourspicehotpot.backend.modules.datban.dto.response.DatBanResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonChiTietResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.LichSuHoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.phieugiamgia.entity.PhieuGiamGia;
import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CheckInService {

    List<CheckInResponse> getDanhSachBanCheckIn();

    CheckInDetailResponse getChiTietCheckIn(Integer idBanAn);

    CheckInResponse xacNhanCheckIn(CheckInRequest request);

    List<SanPhamResponse> getAllMonLe();

    List<SanPhamResponse> getAllSetLau();

    List<SanPhamResponse> getAllThucDon();

    List<SanPhamResponse> searchMonLe(
            String keyword,
            Long idDanhMuc,
            BigDecimal giaMin,
            BigDecimal giaMax
    );

    List<SanPhamResponse> searchSetLau(
            String keyword,
            Long idNuocLau
    );

    HoaDonChiTietResponse themMonVaoHoaDon(
            Long idHoaDon,
            Long idSanPham,
            Integer soLuong,
            String tenBan,
            Long idBanAn
    );

    List<HoaDonChiTietResponse> getMonDaChonTheoHoaDon(Long idHoaDon);

    List<LichSuHoaDonResponse> getLichSuHoaDon(Long idHoaDon);

    // 28/03
    HoaDonChiTietResponse daLenMon(Long idHoaDonChiTiet, String tenBan);

    List<HoaDonChiTietResponse> daLenTatCa(Long idHoaDon, String tenBan);

    void xoaMonKhoiHoaDon(Long idHoaDonChiTiet, String tenBan);


    // giảm giá
    List<PhieuGiamGia> getDanhSachPhieuGiamGiaHopLe(Long idHoaDon);

    HoaDonResponse apDungMaGiamGia(Long idHoaDon, String maGiamGia);

    List<PhieuGiamGia> getDanhSachPhieuGiamGiaTheoKhachHang(Long idHoaDon);

    PhieuGiamGia getPhieuGiamGiaTotNhat(Long idHoaDon);

    // đổi bàn & ghép bàn
    CheckInResponse doiBan(DoiBanRequest request);

    CheckInResponse ghepBan(GhepBanRequest request);

    CheckInResponse tachBan(TachBanRequest request);

    CheckInResponse ghepHaiBanDangDung(GhepBanDangDungRequest request);

    // xem hóa đơn khi gộp bàn đang dùng
    List<HoaDonDangDungTheoBanResponse> getHoaDonDangDungTheoBan(Integer idBanAn);

    CheckInDetailResponse getChiTietCheckInTheoHoaDon(Integer idBanAn, Long idHoaDon);

    // hiển thị bàn trống
    List<BanAnTrongResponse> getBanTrong();

    //tạo hóa đơn mơi trên bàn đag dùng
    CheckInResponse taoHoaDonMoiTrenBan(TaoHoaDonMoiTrenBanRequest request);

    List<DatBanResponse> searchDatBan(String keyword);

    void tuDongDonBan(BanAn ban, HoaDon hoaDon);

    boolean xacThucMaDatBan(Long idDatBan, String maDatBan);

    void autoDonBan();

    List<DatBanResponse> findByNgay(SearchKhachChoCheckInRequest request);
}
