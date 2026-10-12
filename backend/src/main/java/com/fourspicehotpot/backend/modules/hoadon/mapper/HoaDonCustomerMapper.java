package com.fourspicehotpot.backend.modules.hoadon.mapper;

import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.CustomerHoaDonChiTietResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.CustomerHoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDonChiTiet;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HoaDonCustomerMapper {

    public CustomerHoaDonResponse mapToResponse(HoaDon hoaDon) {

        CustomerHoaDonResponse res = new CustomerHoaDonResponse();

        res.setId(hoaDon.getId());
        res.setMaHoaDon(hoaDon.getMaHoaDon());
        res.setLoaiHoaDon(hoaDon.getLoaiHoaDon());
        res.setThoiGianTao(hoaDon.getThoiGianTao());
        res.setThoiGianThanhToan(hoaDon.getThoiGianThanhToan());
        res.setTienCoc(hoaDon.getTienCoc());
        res.setTienThue(hoaDon.getTienThue());
        res.setTenNguoiNhan(hoaDon.getTenNguoiNhan());
        res.setSoDienThoai(hoaDon.getSoDienThoai());
        res.setDiaChiGiao(hoaDon.getDiaChiGiao());
        res.setPhiVanChuyen(hoaDon.getPhiVanChuyen());
        res.setSoTienDuocGiam(hoaDon.getSoTienDuocGiam());
        res.setTongTienMon(hoaDon.getTongTienMon());
        res.setTienSauGiam(hoaDon.getTienSauGiam());
        res.setVatPercent(hoaDon.getVatPercent());
        res.setTienConLai(hoaDon.getTienConLai());
        res.setTongTien(hoaDon.getTongTien());
        res.setTrangThai(hoaDon.getTrangThai());
        res.setGhiChu(hoaDon.getGhiChu());
        res.setNguoiTao(hoaDon.getNguoiTao());
        res.setNgayCapNhat(hoaDon.getNgayCapNhat());
        res.setThoiGianMo(hoaDon.getThoiGianMo());
        res.setThoiGianDong(hoaDon.getThoiGianDong());

        /* ===== Khách hàng ===== */
        if (hoaDon.getKhachHang() != null) {
            res.setIdKhachHang(hoaDon.getKhachHang().getId());
            res.setTenKhachHang(hoaDon.getKhachHang().getTenKhachHang());
            res.setSoDienThoai(hoaDon.getKhachHang().getSoDienThoai());
        }

        /* ===== Nhân viên ===== */
        if (hoaDon.getNhanVien() != null) {
            res.setIdNhanVien(hoaDon.getNhanVien().getId());
            res.setTenNhanVien(hoaDon.getNhanVien().getTenNhanVien());
        }

        /* ===== Bàn ăn ===== */
        if (hoaDon.getBanAns() != null && !hoaDon.getBanAns().isEmpty()) {

            List<Long> ids = hoaDon.getBanAns()
                    .stream()
                    .map(b -> Long.valueOf(b.getId()))
                    .toList();

            List<String> tens = hoaDon.getBanAns()
                    .stream()
                    .map(BanAn::getTenBanAn)
                    .toList();

            res.setListIdBan(ids);
            res.setListTenBan(tens);

            BanAn first = hoaDon.getBanAns().get(0);
            if (first.getKhuVuc() != null) {
                res.setTenTang(first.getKhuVuc().getTenKhuVuc());
            }
        }

        /* ===== Đặt bàn ===== */
        if (hoaDon.getDatBan() != null) {
            res.setIdDatBan(Long.valueOf(hoaDon.getDatBan().getId()));
            res.setHinhThucDat(hoaDon.getDatBan().getHinhThucDat());
        }

        /* ===== Phiếu giảm giá ===== */
        if (hoaDon.getPhieuGiamGia() != null) {
            res.setIdPhieuGiamGia((long) hoaDon.getPhieuGiamGia().getId());
        }

        return res;
    }


    public CustomerHoaDonChiTietResponse mapToChiTietResponse(HoaDonChiTiet ct) {
        return CustomerHoaDonChiTietResponse.builder()
                .id(ct.getId())
                .soLuong(ct.getSoLuong())
                .donGia(ct.getDonGia())
                .thanhTien(ct.getThanhTien())
                .ngayTao(ct.getNgayTao())
                .ngayCapNhat(ct.getNgayCapNhat())
                .trangThai(ct.getTrangThai())
                .idHoaDon(ct.getHoaDon().getId())
                .maHoaDon(ct.getHoaDon().getMaHoaDon())
                .idSanPham(ct.getSanPham().getId())
                .tenSanPham(ct.getSanPham().getTenSanPham())
                .tenDanhMuc(
                        ct.getSanPham().getDanhMuc() != null
                                ? ct.getSanPham().getDanhMuc().getTenDanhMuc()
                                : null
                )
                .hinhAnh(ct.getSanPham().getHinhAnh())
                .build();
    }
}
