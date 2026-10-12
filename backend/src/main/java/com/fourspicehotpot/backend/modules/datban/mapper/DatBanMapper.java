package com.fourspicehotpot.backend.modules.datban.mapper;

import com.fourspicehotpot.backend.integration.mail.service.MailService;
import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.banan.repository.BanAnRepository;
import com.fourspicehotpot.backend.modules.datban.dto.response.DatBanResponse;
import com.fourspicehotpot.backend.modules.datban.entity.DatBan;
import com.fourspicehotpot.backend.modules.datban.repository.DatBanRepository;
import com.fourspicehotpot.backend.modules.datban.service.serviceImpl.DatBanServiceImpl;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonChiTietResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.LichSuHoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonChiTietRepository;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonRepository;
import com.fourspicehotpot.backend.modules.hoadon.repository.LichSuHoaDonRepository;
import com.fourspicehotpot.backend.modules.hoadon.service.LichSuHoaDonService;

import com.fourspicehotpot.backend.modules.khachhang.repository.KhachHangRepository;
import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import com.fourspicehotpot.backend.modules.nhanvien.repository.NhanVienRepository;
import com.fourspicehotpot.backend.modules.setlau.repository.SanPhamRepository;
import com.fourspicehotpot.backend.modules.thamsohethong.service.ThamSoHeThongService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DatBanMapper {

    private static final Logger log = LoggerFactory.getLogger(DatBanServiceImpl.class);

    private final DatBanRepository datBanRepository;
    private final KhachHangRepository khachHangRepository;
    private final BanAnRepository banAnRepository;
    private final HoaDonRepository hoaDonRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final ThamSoHeThongService thamSoHeThongService;
    private final SanPhamRepository sanPhamRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SpringTemplateEngine templateEngine;
    private final MailService mailService;
    private final LichSuHoaDonService lichSuHoaDonService;
    private final NhanVienRepository nhanVienRepository;



    public DatBanResponse mapToResponse(DatBan entity) {
        List<Integer> listIdBan = null;
        List<String> listTenBan = null;
        String tenKhuVuc = null;


        if (entity.getBanAns() != null && !entity.getBanAns().isEmpty()) {

            listIdBan = entity.getBanAns()
                    .stream()
                    .map(BanAn::getId)
                    .toList();

            listTenBan = entity.getBanAns()
                    .stream()
                    .map(BanAn::getTenBanAn)
                    .toList();

            // lấy khu vực theo bàn đầu
            BanAn first = entity.getBanAns().get(0);
            if (first.getKhuVuc() != null) {
                tenKhuVuc = first.getKhuVuc().getTenKhuVuc();
            }
        }

        HoaDon hoaDonEntity = hoaDonRepository.findByDatBan(entity)
                .orElse(null);
        HoaDonResponse hoaDonResponse = null;
        List<HoaDonChiTietResponse> chiTietHoaDons = new ArrayList<>();
        List<LichSuHoaDonResponse> lichSuHoaDons = new ArrayList<>();


        if (hoaDonEntity != null) {
            hoaDonResponse = HoaDonResponse.builder()
                    .id(hoaDonEntity.getId())
                    .trangThai(hoaDonEntity.getTrangThai())
                    .tienCoc(hoaDonEntity.getTienCoc())
                    .build();

            chiTietHoaDons = hoaDonChiTietRepository.findByHoaDon(hoaDonEntity)
                    .stream()
                    .map(ct -> HoaDonChiTietResponse.builder()
                            .tenSanPham(ct.getSanPham().getTenSanPham())
                            .soLuong(ct.getSoLuong())
                            .donGia(ct.getDonGia())
                            .thanhTien(ct.getThanhTien())
                            .hinhAnh(ct.getSanPham().getHinhAnh())
                            .build()
                    )
                    .toList();

            lichSuHoaDons = lichSuHoaDonRepository.findByHoaDon(hoaDonEntity)
                    .stream()
                    .map(ls -> LichSuHoaDonResponse.builder()
                            .tenNhanVien(
                                    Optional.ofNullable(ls.getNhanVien())
                                            .map(NhanVien::getTenNhanVien)
                                            .orElse("Hệ thống")
                            )
                            .hanhDong(ls.getHanhDong())
                            .thoiGian(ls.getThoiGian())
                            .ghiChu(ls.getGhiChu())
                            .build()
                    )
                    .toList();
        }
        return DatBanResponse.builder()
                .id(Math.toIntExact(entity.getId()))
                .maDatBan(entity.getMaDatBan())

                .idKhachHang(
                        entity.getKhachHang() != null ? entity.getKhachHang().getId() : null
                )
                .maKhachHang(entity.getKhachHang().getMaKhachHang())
                .tenKhachHang(entity.getKhachHang().getTenKhachHang())
                .soDienThoai(entity.getKhachHang().getSoDienThoai())

                .listIdBan(listIdBan)
                .listTenBan(listTenBan)
                .tenKhuVuc(tenKhuVuc)

                .tienCoc(entity.getTienCoc())
                .phanTramCoc(entity.getPhanTramCoc())
                .hanGiuBan(entity.getHanGiuBan())
                .hanThanhToanCoc(entity.getHanThanhToanCoc())
                .trangThaiCoc(entity.getTrangThaiCoc())

                .thoiGianDat(entity.getThoiGianDat())
                .soNguoiToiDa(entity.getSoNguoiToiDa())
                .ghiChu(entity.getGhiChu())
                .hinhThucDat(entity.getHinhThucDat())
                .trangThai(entity.getTrangThai())
                .ngayTao(entity.getNgayTao())
                .ngayCapNhat(entity.getNgayCapNhat())
                .hanThanhToanCoc(entity.getHanThanhToanCoc())

                .hoaDon(hoaDonResponse)
                .chiTietHoaDons(chiTietHoaDons)
                .lichSuHoaDons(lichSuHoaDons)

                .build();
    }

    public DatBanResponse mapToResponseHienThi(DatBan entity) {
        List<Integer> listIdBan = null;
        List<String> listTenBan = null;
        String tenKhuVuc = null;


        if (entity.getBanAns() != null && !entity.getBanAns().isEmpty()) {

            listIdBan = entity.getBanAns()
                    .stream()
                    .map(BanAn::getId)
                    .toList();

            listTenBan = entity.getBanAns()
                    .stream()
                    .map(BanAn::getTenBanAn)
                    .toList();

            // lấy khu vực theo bàn đầu
            BanAn first = entity.getBanAns().get(0);
            if (first.getKhuVuc() != null) {
                tenKhuVuc = first.getKhuVuc().getTenKhuVuc();
            }
        }

        return DatBanResponse.builder()
                .id(Math.toIntExact(entity.getId()))
                .maDatBan(entity.getMaDatBan())

                .idKhachHang(
                        entity.getKhachHang() != null ? entity.getKhachHang().getId() : null
                )
                .maKhachHang(entity.getKhachHang().getMaKhachHang())
                .tenKhachHang(entity.getKhachHang().getTenKhachHang())
                .soDienThoai(entity.getKhachHang().getSoDienThoai())

                .listIdBan(listIdBan)
                .listTenBan(listTenBan)
                .tenKhuVuc(tenKhuVuc)

                .tienCoc(entity.getTienCoc())
                .phanTramCoc(entity.getPhanTramCoc())
                .hanGiuBan(entity.getHanGiuBan())
                .hanThanhToanCoc(entity.getHanThanhToanCoc())
                .trangThaiCoc(entity.getTrangThaiCoc())

                .thoiGianDat(entity.getThoiGianDat())
                .soNguoiToiDa(entity.getSoNguoiToiDa())
                .ghiChu(entity.getGhiChu())
                .hinhThucDat(entity.getHinhThucDat())
                .trangThai(entity.getTrangThai())
                .ngayTao(entity.getNgayTao())
                .ngayCapNhat(entity.getNgayCapNhat())
                .build();
    }


}
