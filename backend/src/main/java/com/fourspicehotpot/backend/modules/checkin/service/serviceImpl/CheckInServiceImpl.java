package com.fourspicehotpot.backend.modules.checkin.service.serviceImpl;

import com.fourspicehotpot.backend.common.enums.ThamSoHeThong;
import com.fourspicehotpot.backend.modules.datban.dto.response.DatBanResponse;
import com.fourspicehotpot.backend.modules.datban.mapper.DatBanMapper;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonChiTietResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.LichSuHoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDonBanAn;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDonChiTiet;
import com.fourspicehotpot.backend.modules.hoadon.entity.LichSuHoaDon;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonBanAnRepository;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonChiTietRepository;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonRepository;
import com.fourspicehotpot.backend.modules.hoadon.repository.LichSuHoaDonRepository;
import com.fourspicehotpot.backend.modules.thamsohethong.service.ThamSoHeThongService;
import com.fourspicehotpot.backend.common.enums.TrangThaiBan;
import com.fourspicehotpot.backend.common.enums.TrangThaiDatBan;
import com.fourspicehotpot.backend.common.enums.TrangThaiHoaDon;
import com.fourspicehotpot.backend.common.util.DateTimeUtils;
import com.fourspicehotpot.backend.integration.websocket.service.WebSocketService;
import com.fourspicehotpot.backend.integration.pdf.utils.HoaDonPdf;
import com.fourspicehotpot.backend.integration.pdf.utils.GoiMonCheckInPdf;
import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.banan.repository.BanAnRepository;
import com.fourspicehotpot.backend.modules.checkin.dto.request.*;
import com.fourspicehotpot.backend.modules.checkin.dto.response.BanAnTrongResponse;
import com.fourspicehotpot.backend.modules.checkin.dto.response.CheckInDetailResponse;
import com.fourspicehotpot.backend.modules.checkin.dto.response.CheckInResponse;
import com.fourspicehotpot.backend.modules.checkin.dto.response.HoaDonDangDungTheoBanResponse;
import com.fourspicehotpot.backend.modules.checkin.service.CheckInService;
import com.fourspicehotpot.backend.modules.datban.entity.DatBan;
import com.fourspicehotpot.backend.modules.datban.repository.DatBanRepository;
import com.fourspicehotpot.backend.modules.dotgiamgia.entity.DotGiamGia;
import com.fourspicehotpot.backend.modules.dotgiamgia.entity.DotGiamGiaSanPham;
import com.fourspicehotpot.backend.modules.dotgiamgia.repository.DotGiamGiaSanPhamRepository;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.khachhang.repository.KhachHangRepository;
import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import com.fourspicehotpot.backend.modules.nhanvien.repository.NhanVienRepository;
import com.fourspicehotpot.backend.modules.phieugiamgia.dto.response.PhieuGiamGiaResponse;
import com.fourspicehotpot.backend.modules.phieugiamgia.entity.KhachHangGiamGia;
import com.fourspicehotpot.backend.modules.phieugiamgia.entity.PhieuGiamGia;
import com.fourspicehotpot.backend.modules.phieugiamgia.repository.KhachHangGiamGiaRepository;
import com.fourspicehotpot.backend.modules.phieugiamgia.repository.PhieuGiamGiaRepository;
import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import com.fourspicehotpot.backend.modules.setlau.repository.SanPhamRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class CheckInServiceImpl implements CheckInService {

    @Autowired
    private BanAnRepository quanLyBanRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private DatBanRepository datBanRepository;

    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private HoaDonBanAnRepository hoaDonBanAnRepository;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Autowired
    private ThamSoHeThongService thamSoHeThongService;

    @Autowired
    private KhachHangGiamGiaRepository khachHangGiamGiaRepository;

    @Autowired
    private DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private GoiMonCheckInPdf goiMonCheckInPdf;
    @Autowired
    private DatBanMapper datBanMapper;

    private boolean conHanGiuBan(DatBan datBan) {
        if (datBan == null) return false;

        LocalDateTime hanGiuBan = tinhHanGiuBan(datBan);
        if (hanGiuBan == null) return false;

        return DateTimeUtils.now().isBefore(hanGiuBan);
    }

    private void xuLyDatBanHetHanNeuCan(DatBan datBan) {
        if (datBan == null) return;

        LocalDateTime hanGiuBan = tinhHanGiuBan(datBan);
        if (hanGiuBan == null) return;

        if (DateTimeUtils.now().isBefore(hanGiuBan)) return;

        if (TrangThaiDatBan.HOAN_THANH.getCode().equals(datBan.getTrangThai())
                || TrangThaiDatBan.DA_HUY.getCode().equals(datBan.getTrangThai())
                || TrangThaiDatBan.KHONG_DEN.getCode().equals(datBan.getTrangThai())
                || TrangThaiDatBan.HET_HAN.getCode().equals(datBan.getTrangThai())
                || TrangThaiDatBan.DANG_PHUC_VU.getCode().equals(datBan.getTrangThai())) {
            return;
        }

        log.warn("[CHECKIN_EXPIRE] Đặt bàn id={} - maDatBan={} đã hết hạn giữ bàn",
                datBan.getId(), datBan.getMaDatBan());

        // 1. Đặt bàn -> HẾT HẠN
        datBan.setTrangThai(TrangThaiDatBan.HET_HAN.getCode());
        datBan.setNgayCapNhat(DateTimeUtils.now());
        datBanRepository.save(datBan);

        // GỬI WS ĐẶT BÀN
        webSocketService.sendDatBanUpdate(datBan);

        // 2. Hóa đơn -> HỦY
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findByDatBan(datBan);
        if (optionalHoaDon.isPresent()) {
            HoaDon hoaDon = optionalHoaDon.get();

            if (!Objects.equals(hoaDon.getTrangThai(), TrangThaiHoaDon.DA_THANH_TOAN.getCode())
                    && !Objects.equals(hoaDon.getTrangThai(), TrangThaiHoaDon.HOAN_THANH.getCode())
                    && !Objects.equals(hoaDon.getTrangThai(), TrangThaiHoaDon.DA_HUY.getCode())) {

                hoaDon.setTrangThai(TrangThaiHoaDon.DA_HUY.getCode());
                hoaDon.setNgayCapNhat(DateTimeUtils.now());
                hoaDon.setThoiGianDong(DateTimeUtils.now());
                hoaDonRepository.save(hoaDon);

                hoaDonBanAnRepository.deleteByHoaDon_Id(hoaDon.getId());

                lichSuHoaDonRepository.save(
                        LichSuHoaDon.builder()
                                .hoaDon(hoaDon)
                                .thoiGian(DateTimeUtils.now())
                                .ngayTao(DateTimeUtils.now())
                                .ngayCapNhat(DateTimeUtils.now())
                                .hanhDong("HUY_DO_HET_HAN_GIU_BAN")
                                .trangThai(1)
                                .ghiChu("Hóa đơn bị hủy do hết hạn")
                                .nhanVien(hoaDon.getNhanVien())
                                .build()
                );

                // GỬI WS HÓA ĐƠN
                webSocketService.sendHoaDonUpdate(hoaDon);
            }
        }

        // 3. Bàn -> TRỐNG
        if (datBan.getBanAns() != null) {
            for (BanAn ban : datBan.getBanAns()) {
                ban.setTrangThai(TrangThaiBan.TRONG.getValue());
                ban.setNgayCapNhat(DateTimeUtils.now());
                quanLyBanRepository.saveAndFlush(ban);
            }
        }

        // GỬI WS LẦN CUỐI (để FE reload grid)
        webSocketService.sendDatBanUpdate(datBan);
    }

    private LocalDateTime tinhHanGiuBan(DatBan datBan) {
        if (datBan == null) return null;

        // nếu DB đã có hanGiuBan thì ưu tiên dùng luôn
        if (datBan.getHanGiuBan() != null) {
            return datBan.getHanGiuBan();
        }

        if (datBan.getThoiGianDat() == null) {
            return null;
        }

        Integer thoiGianGiuBan = thamSoHeThongService.getInt("THOI_GIAN_GIU_BAN");
        if (thoiGianGiuBan == null || thoiGianGiuBan <= 0) {
            return datBan.getThoiGianDat();
        }

        return datBan.getThoiGianDat().plusMinutes(thoiGianGiuBan);
    }

    private PhieuGiamGiaResponse mapToPhieuGiamGiaResponse(PhieuGiamGia p) {
        if (p == null) return null;

        return PhieuGiamGiaResponse.builder()
                .id(p.getId())
                .maGiamGia(p.getMaGiamGia())
                .tenGiamGia(p.getTenGiamGia())
                .phuongThucGiamGia(p.getPhuongThucGiamGia())
                .giaTriGiam(p.getGiaTriGiam())
                .giamToiDa(p.getGiamToiDa())
                .dieuKienSuDung(p.getDieuKienSuDung())
                .ngayBatDau(p.getNgayBatDau())
                .ngayKetThuc(p.getNgayKetThuc())
                .soLuong(p.getSoLuong())
                .kieuGiamGia(p.getKieuGiamGia())
                .build();
    }

    @Override
    public List<CheckInResponse> getDanhSachBanCheckIn() {
        log.info("=== BẮT ĐẦU getDanhSachBanCheckIn ===");

        List<BanAn> dsBan = quanLyBanRepository.findAll();

        return dsBan.stream().map(ban -> {
            try {
                List<Integer> trangThaiDangHoatDong = List.of(
                        TrangThaiHoaDon.VUA_TAO.getCode(),
                        TrangThaiHoaDon.CHO_COC.getCode(),
                        TrangThaiHoaDon.DA_COC.getCode(),
                        TrangThaiHoaDon.DA_XAC_NHAN.getCode(),
                        TrangThaiHoaDon.KHACH_DEN.getCode(),
                        TrangThaiHoaDon.DANG_PHUC_VU.getCode(),
                        TrangThaiHoaDon.CHO_THANH_TOAN.getCode()
                );

                List<HoaDonBanAn> dsLienKet = hoaDonBanAnRepository.findHoaDonDangHoatDongByBan(
                        ban.getId(),
                        trangThaiDangHoatDong
                );

                int soHoaDonDangHoatDong = dsLienKet.size();
                boolean coNhieuHoaDon = soHoaDonDangHoatDong >= 2;
                int sucChua = ban.getSucChuaToiDa() != null ? ban.getSucChuaToiDa() : 0;

                CheckInResponse.CheckInResponseBuilder builder = CheckInResponse.builder()
                        .idBanAn(ban.getId())
                        .maBanAn(ban.getMaBanAn())
                        .tenBanAn(ban.getTenBanAn())
                        .sucChuaToiDa(sucChua)
                        .khuVucId(ban.getKhuVuc() != null ? ban.getKhuVuc().getId() : null)
                        .tenKhuVuc(ban.getKhuVuc() != null ? ban.getKhuVuc().getTenKhuVuc() : null)
                        .soHoaDonDangHoatDong(soHoaDonDangHoatDong)
                        .coNhieuHoaDon(coNhieuHoaDon)
                        .laBanDungChungNhieuHoaDon(coNhieuHoaDon);

                // --- BƯỚC 1: LỌC HÓA ĐƠN THEO TƯ DUY "THOÁNG" CỦA MÀY ---
                List<HoaDonBanAn> dangNgoiThucTe = new ArrayList<>();

                for (HoaDonBanAn lk : dsLienKet) {
                    HoaDon hoaDonLienKet = lk.getHoaDon();

                    if (hoaDonLienKet.getDatBan() != null) {
                        String ttHoaDon = hoaDonLienKet.getTrangThai() != null ? String.valueOf(hoaDonLienKet.getTrangThai()) : "";
                        if (!ttHoaDon.equals(String.valueOf(TrangThaiHoaDon.DANG_PHUC_VU.getCode())) &&
                                !ttHoaDon.equals(String.valueOf(TrangThaiHoaDon.CHO_THANH_TOAN.getCode()))) {
                            continue;
                        }
                    }

                    dangNgoiThucTe.add(lk);
                }

                // --- BƯỚC 2: XÉT TRẠNG THÁI HIỂN THỊ THEO DỮ LIỆU ĐÃ LỌC ---
                if (!dangNgoiThucTe.isEmpty()) {
                    // NHÁNH 1: ĐANG CÓ NGƯỜI NGỒI THẬT SỰ TẠI BÀN -> BÁO ĐỎ (ĐANG SỬ DỤNG)
                    HoaDon hdHienTai = dangNgoiThucTe.get(0).getHoaDon();

                    int tongKhachThucTe = dangNgoiThucTe.stream()
                            .mapToInt(lienKet -> lienKet.getSoKhach() != null ? lienKet.getSoKhach() : 0).sum();
                    int soGheTrongThucTe = Math.max(sucChua - tongKhachThucTe, 0);

                    List<Integer> listIdBan = layListIdBanTheoHoaDon(hdHienTai.getId());
                    List<String> listTenBan = layListTenBanTheoHoaDon(hdHienTai.getId());

                    builder.trangThaiBan(TrangThaiBan.DANG_SU_DUNG.getValue())
                            .soNguoiDangDung(tongKhachThucTe)
                            .soGheTrong(soGheTrongThucTe)
                            .idHoaDon(hdHienTai.getId())
                            .maHoaDon(hdHienTai.getMaHoaDon())
                            .trangThaiHoaDon(hdHienTai.getTrangThai())
                            .listIdBan(listIdBan)
                            .listTenBan(listTenBan);

                    if (hdHienTai.getDatBan() != null) {
                        DatBan db = hdHienTai.getDatBan();
                        builder.idDatBan(db.getId())
                                .maDatBan(db.getMaDatBan())
                                .thoiGianDat(db.getThoiGianDat())
                                .soNguoiToiDa(db.getSoNguoiToiDa())
                                .trangThaiDatBan(db.getTrangThai())
                                .ghiChuDatBan(db.getGhiChu())
                                .hanGiuBan(tinhHanGiuBan(db))
                                .trangThaiCoc(db.getTrangThaiCoc())
                                .tenKhachHang(db.getKhachHang() != null ? db.getKhachHang().getTenKhachHang() : null)
                                .soDienThoai(db.getKhachHang() != null ? db.getKhachHang().getSoDienThoai() : null)
                                .hinhThucDat(db.getHinhThucDat());
                    }
                } else {
                    // NHÁNH 2: BÀN CHƯA CÓ NGƯỜI NGỒI -> TÌM PHIẾU ĐẶT BÀN CHỜ CHECK-IN
                    DatBan datBan = datBanRepository.findTopByBanAns_IdAndTrangThai(Long.valueOf(ban.getId()), TrangThaiDatBan.DA_XAC_NHAN.getCode());
                    if (datBan == null)
                        datBan = datBanRepository.findTopByBanAns_IdAndTrangThai(Long.valueOf(ban.getId()), TrangThaiDatBan.DA_COC.getCode());
                    if (datBan == null)
                        datBan = datBanRepository.findTopByBanAns_IdAndTrangThai(Long.valueOf(ban.getId()), TrangThaiDatBan.CHO_XAC_NHAN.getCode());
                    if (datBan == null)
                        datBan = datBanRepository.findTopByBanAns_IdAndTrangThai(Long.valueOf(ban.getId()), TrangThaiDatBan.CHUA_COC.getCode());

                    boolean dangDon = ban.getTrangThai() != null && ban.getTrangThai() == TrangThaiBan.DANG_DON.getValue();

                    if (datBan != null) {
                        xuLyDatBanHetHanNeuCan(datBan);
                        LocalDateTime bayGio = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

                        Integer datTruocToiThieu = thamSoHeThongService.getInt(ThamSoHeThong.DAT_TRUOC_TOI_THIEU.getCode());
                        if (datTruocToiThieu == null) datTruocToiThieu = 60;

                        // Mốc để quyết định màu sắc: Nếu cách giờ đặt > datTruocToiThieu thì hiện màu Xanh lá (Trống)
                        boolean hienMauTrong = datBan.getThoiGianDat() != null &&
                                datBan.getThoiGianDat().isAfter(bayGio.plusMinutes(datTruocToiThieu));

                        if (!conHanGiuBan(datBan)) {
                            builder.trangThaiBan(dangDon ? TrangThaiBan.DANG_DON.getValue() : TrangThaiBan.TRONG.getValue()).idDatBan(null).thoiGianDat(null);
                        } else if (hienMauTrong) {
                            // TRÊN 1 TIẾNG: HIỆN MÀU XANH LÁ (Trống) nhưng vẫn gửi thoiGianDat về cho Vue
                            builder.trangThaiBan(dangDon ? TrangThaiBan.DANG_DON.getValue() : TrangThaiBan.TRONG.getValue())
                                    .thoiGianDat(datBan.getThoiGianDat())
                                    .idDatBan(datBan.getId())
                                    .hinhThucDat(datBan.getHinhThucDat())
                                    .message("Khách đặt lúc " + datBan.getThoiGianDat().toLocalTime());
                        } else {
                            // DƯỚI 1 TIẾNG: ƯU TIÊN HIỆN ĐANG DỌN ĐỂ NHÂN VIÊN DỌN BÀN NHANH HOẶC HIỆN ĐÃ ĐẶT
                            builder.trangThaiBan(dangDon ? TrangThaiBan.DANG_DON.getValue() : TrangThaiBan.DA_DAT.getValue())
                                    .thoiGianDat(datBan.getThoiGianDat())
                                    .hinhThucDat(datBan.getHinhThucDat())
                                    .idDatBan(datBan.getId());
                        }
                    } else {
                        // NHÁNH 3: KHÔNG CÓ HÓA ĐƠN NÀO + KHÔNG CÓ LỊCH ĐẶT NÀO => TRỐNG (Ngoại trừ Đang dọn hoặc Không sử dụng)
                        boolean khongSuDung = ban.getTrangThai() != null && ban.getTrangThai() == TrangThaiBan.KHONG_SU_DUNG.getValue();
                        Integer finalTrangThai = khongSuDung ? TrangThaiBan.KHONG_SU_DUNG.getValue() : (dangDon ? TrangThaiBan.DANG_DON.getValue() : TrangThaiBan.TRONG.getValue());
                        
                        builder.trangThaiBan(finalTrangThai)
                                .idHoaDon(null).maHoaDon(null).trangThaiHoaDon(null)
                                .idDatBan(null).maDatBan(null).thoiGianDat(null).soNguoiToiDa(null)
                                .trangThaiDatBan(null).ghiChuDatBan(null).tenKhachHang(null).soDienThoai(null);
                    }
                }

                return builder.build();

            } catch (Exception e) {
                log.error("Lỗi khi load check-in cho bàn id={}: {}", ban.getId(), e.getMessage(), e);
                return CheckInResponse.builder()
                        .idBanAn(ban.getId())
                        .maBanAn(ban.getMaBanAn())
                        .tenBanAn(ban.getTenBanAn())
                        .trangThaiBan(ban.getTrangThai())
                        .message("Lỗi load dữ liệu check-in: " + e.getMessage())
                        .build();
            }
        }).toList();
    }

    @Override
    public CheckInDetailResponse getChiTietCheckIn(Integer idBanAn) {

        log.info("=== BẮT ĐẦU getChiTietCheckIn | idBanAn={} ===", idBanAn);

        BanAn banAn = quanLyBanRepository.findById(idBanAn)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy bàn ăn với id={}", idBanAn);
                    return new RuntimeException("Không tìm thấy bàn ăn");
                });

        HoaDon hoaDonDangMo = hoaDonBanAnRepository.findTopHoaDonDangHoatDongTheoBan(idBanAn)
                .map(HoaDonBanAn::getHoaDon)
                .orElse(null);

        List<Integer> trangThaiDangHoatDong = List.of(
                TrangThaiHoaDon.VUA_TAO.getCode(),
                TrangThaiHoaDon.CHO_COC.getCode(),
                TrangThaiHoaDon.DA_COC.getCode(),
                TrangThaiHoaDon.DA_XAC_NHAN.getCode(),
                TrangThaiHoaDon.KHACH_DEN.getCode(),
                TrangThaiHoaDon.DANG_PHUC_VU.getCode(),
                TrangThaiHoaDon.CHO_THANH_TOAN.getCode()
        );

        List<HoaDonBanAn> dsHoaDonDangHoatDong =
                hoaDonBanAnRepository.findHoaDonDangHoatDongByBan(idBanAn, trangThaiDangHoatDong);

        int soHoaDonDangHoatDong = dsHoaDonDangHoatDong.size();
        boolean coNhieuHoaDon = soHoaDonDangHoatDong >= 2;

        if (hoaDonDangMo != null) {
            log.info("Tìm thấy hóa đơn đang hoạt động id={} qua bảng trung gian cho bàn {}",
                    hoaDonDangMo.getId(), idBanAn);
        } else {
            log.warn("Không tìm thấy hóa đơn đang hoạt động qua bảng trung gian cho bàn {}", idBanAn);
        }

        DatBan datBan = null;

        if (hoaDonDangMo != null && hoaDonDangMo.getDatBan() != null) {
            datBan = hoaDonDangMo.getDatBan();
        } else {
            datBan = datBanRepository.findTopByBanAns_IdAndTrangThai(
                    Long.valueOf(idBanAn),
                    TrangThaiDatBan.DA_XAC_NHAN.getCode()
            );

            if (datBan == null) {
                datBan = datBanRepository.findTopByBanAns_IdAndTrangThai(
                        Long.valueOf(idBanAn),
                        TrangThaiDatBan.DA_COC.getCode()
                );
            }
            if (datBan == null) {
                datBan = datBanRepository.findTopByBanAns_IdAndTrangThai(
                        Long.valueOf(idBanAn),
                        TrangThaiDatBan.CHO_XAC_NHAN.getCode()
                );
            }

            if (datBan == null) {
                datBan = datBanRepository.findTopByBanAns_IdAndTrangThai(
                        Long.valueOf(idBanAn),
                        TrangThaiDatBan.CHUA_COC.getCode()
                );
            }
        }

        if (datBan != null) {
            xuLyDatBanHetHanNeuCan(datBan);

            if (!conHanGiuBan(datBan)
                    && !TrangThaiDatBan.DANG_PHUC_VU.getCode().equals(datBan.getTrangThai())) {
                datBan = null;
            }
        }

        List<Integer> listIdBan = hoaDonDangMo != null
                ? layListIdBanTheoHoaDon(hoaDonDangMo.getId())
                : List.of();

        List<String> listTenBan = hoaDonDangMo != null
                ? layListTenBanTheoHoaDon(hoaDonDangMo.getId())
                : List.of();

        Integer soKhachThucTe = dsHoaDonDangHoatDong.stream()
                .mapToInt(lienKet -> lienKet.getSoKhach() != null ? lienKet.getSoKhach() : 0)
                .sum();
                
        Integer tongKhachHoaDon = soKhachThucTe;
        if (hoaDonDangMo != null) {
            tongKhachHoaDon = hoaDonBanAnRepository.findByHoaDon_Id(hoaDonDangMo.getId()).stream()
                    .mapToInt(lk -> lk.getSoKhach() != null ? lk.getSoKhach() : 0)
                    .sum();
        }

        return CheckInDetailResponse.builder()
                .idBan(banAn.getId())
                .maBan(banAn.getMaBanAn())
                .tenBan(banAn.getTenBanAn())
                .sucChuaToiDa(banAn.getSucChuaToiDa())
                .trangThaiBan(banAn.getTrangThai())
                .idKhuVuc(banAn.getKhuVuc() != null ? banAn.getKhuVuc().getId() : null)
                .tenKhuVuc(banAn.getKhuVuc() != null ? banAn.getKhuVuc().getTenKhuVuc() : null)

                .idDatBan(datBan != null ? datBan.getId() : null)
                .maDatBan(datBan != null ? datBan.getMaDatBan() : null)
                .thoiGianDat(datBan != null ? datBan.getThoiGianDat() : null)
                .soNguoiDat(tongKhachHoaDon != null && tongKhachHoaDon > 0 ? tongKhachHoaDon : (datBan != null ? datBan.getSoNguoiToiDa() : 0))
                .soNguoiDangDung(soKhachThucTe)
                .ghiChuDatBan(datBan != null ? datBan.getGhiChu() : null)
                .tienCoc(hoaDonDangMo != null && hoaDonDangMo.getTienCoc() != null 
                        ? hoaDonDangMo.getTienCoc() 
                        : (datBan != null ? datBan.getTienCoc() : null))
                .trangThaiCoc(datBan != null ? datBan.getTrangThaiCoc() : null)

                .trangThaiDatBan(datBan != null ? datBan.getTrangThai() : null)
                .hinhThucDat(datBan != null ? datBan.getHinhThucDat() : null)
                .ngayTaoDatBan(datBan != null ? datBan.getNgayTao() : null)
                .ngayCapNhatDatBan(datBan != null ? datBan.getNgayCapNhat() : null)
                .hanGiuBan(datBan != null ? tinhHanGiuBan(datBan) : null)
                .hanThanhToanCoc(datBan != null ? datBan.getHanThanhToanCoc() : null)

                .idKhachHang(datBan != null && datBan.getKhachHang() != null ? datBan.getKhachHang().getId() : null)
                .tenKhachHang(datBan != null && datBan.getKhachHang() != null ? datBan.getKhachHang().getTenKhachHang() : null)
                .soDienThoai(datBan != null && datBan.getKhachHang() != null ? datBan.getKhachHang().getSoDienThoai() : null)

                .idHoaDon(hoaDonDangMo != null ? hoaDonDangMo.getId() : null)
                .maHoaDon(hoaDonDangMo != null ? hoaDonDangMo.getMaHoaDon() : null)
                .trangThaiHoaDon(hoaDonDangMo != null ? hoaDonDangMo.getTrangThai() : null)

                .thoiGianCheckIn(hoaDonDangMo != null ? hoaDonDangMo.getThoiGianMo() : null)
                .thoiGianTaoHoaDon(hoaDonDangMo != null ? hoaDonDangMo.getThoiGianTao() : null)
                .thoiGianMo(hoaDonDangMo != null ? hoaDonDangMo.getThoiGianMo() : null)
                .thoiGianThanhToan(hoaDonDangMo != null ? hoaDonDangMo.getThoiGianThanhToan() : null)
                .thoiGianDong(hoaDonDangMo != null ? hoaDonDangMo.getThoiGianDong() : null)
                .idNhanVien(hoaDonDangMo != null && hoaDonDangMo.getNhanVien() != null ? hoaDonDangMo.getNhanVien().getId() : null)
                .tenNhanVien(hoaDonDangMo != null && hoaDonDangMo.getNhanVien() != null ? hoaDonDangMo.getNhanVien().getTenNhanVien() : null)

                .tongTienMon(hoaDonDangMo != null ? hoaDonDangMo.getTongTienMon() : null)
                .tongTien(hoaDonDangMo != null ? hoaDonDangMo.getTongTien() : null)
                .soTienDuocGiam(hoaDonDangMo != null ? hoaDonDangMo.getSoTienDuocGiam() : null)
                .tienSauGiam(hoaDonDangMo != null ? hoaDonDangMo.getTienSauGiam() : null)
                .tienConLai(hoaDonDangMo != null ? hoaDonDangMo.getTienConLai() : null)
                .tongTienDaThanhToan(hoaDonDangMo != null ? hoaDonDangMo.getTongTienDaThanhToan() : null)
                .tongTienDaThanhToan(hoaDonDangMo != null ? hoaDonDangMo.getTongTienDaThanhToan() : null)
                .idPhieuGiamGia(
                        hoaDonDangMo != null && hoaDonDangMo.getPhieuGiamGia() != null
                                ? Long.valueOf(hoaDonDangMo.getPhieuGiamGia().getId())
                                : null
                )
                .maGiamGia(
                        hoaDonDangMo != null && hoaDonDangMo.getPhieuGiamGia() != null
                                ? hoaDonDangMo.getPhieuGiamGia().getMaGiamGia()
                                : null
                )
                .tenGiamGia(
                        hoaDonDangMo != null && hoaDonDangMo.getPhieuGiamGia() != null
                                ? hoaDonDangMo.getPhieuGiamGia().getTenGiamGia()
                                : null
                )
                .phieuGiamGia(
                        hoaDonDangMo != null && hoaDonDangMo.getPhieuGiamGia() != null
                                ? mapToPhieuGiamGiaResponse(hoaDonDangMo.getPhieuGiamGia())
                                : null
                )
                .listIdBan(listIdBan)
                .listTenBan(listTenBan)
                .soHoaDonDangHoatDong(soHoaDonDangHoatDong)
                .coNhieuHoaDon(coNhieuHoaDon)
                .laBanDungChungNhieuHoaDon(coNhieuHoaDon)
                .build();
    }

    private String generateMaHoaDon() {
        java.util.Random random = new java.util.Random();
        int num4 = random.nextInt(10000);
        char letter = (char) ('A' + random.nextInt(26));
        int num1 = random.nextInt(10);
        return String.format("HDTQ-%04d%c%d", num4, letter, num1);
    }

    private String generateMaDatBanQuay() {
        java.util.Random random = new java.util.Random();
        char letter1 = (char) ('A' + random.nextInt(26));
        int num1 = random.nextInt(10);
        char letter2 = (char) ('A' + random.nextInt(26));
        int num2 = random.nextInt(10);
        return String.format("DBTQ-%c%d%c%d", letter1, num1, letter2, num2);
    }

    private void validateDaToiGioCheckIn(DatBan datBan) {
        if (datBan == null || datBan.getThoiGianDat() == null) {
            return;
        }

        LocalDateTime now = DateTimeUtils.now();
        LocalDateTime thoiGianDat = datBan.getThoiGianDat();

        if (now.isBefore(thoiGianDat)) {
            throw new RuntimeException(
                    "Chưa tới giờ đặt bàn, không thể check-in. Giờ đặt bàn: "
                            + thoiGianDat
            );
        }
    }

    @Override
    @Transactional
    public CheckInResponse xacNhanCheckIn(CheckInRequest request) {

        log.info("=== BẮT ĐẦU xacNhanCheckIn ===");
        log.info("[CHECKIN_XACNHAN] request={}", request);

        try {
            // 0. VALIDATE REQUEST
            if (request == null) {
                throw new RuntimeException("Request check-in không được để null");
            }

            if (request.getIdBanAn() == null) {
                throw new RuntimeException("Thiếu id bàn");
            }

            if (request.getIdNhanVien() == null) {
                throw new RuntimeException("Thiếu id nhân viên");
            }

            if (request.getSoKhach() == null || request.getSoKhach() <= 0) {
                throw new RuntimeException("Vui lòng nhập số khách hợp lệ");
            }

            log.info("Request: idDatBan={}, idBanAn={}, idNhanVien={}, soKhach={}, ghiChu={}",
                    request.getIdDatBan(),
                    request.getIdBanAn(),
                    request.getIdNhanVien(),
                    request.getSoKhach(),
                    request.getGhiChu());

            // 1. LẤY BÀN + NHÂN VIÊN
            BanAn banAn = quanLyBanRepository.findById(request.getIdBanAn())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn"));

            log.info("Tìm thấy bàn click: id={}, tên={}, trạng thái={}",
                    banAn.getId(), banAn.getTenBanAn(), banAn.getTrangThai());

            // KHOÁ BÀN LẠI DƯỚI DB (Tránh 2 nhân viên hoặc click đúp cùng lúc dẫn đến tạo 2 hóa đơn)
            banAn.setNgayCapNhat(DateTimeUtils.now());
            quanLyBanRepository.saveAndFlush(banAn);

            NhanVien nhanVien = nhanVienRepository.findById(request.getIdNhanVien())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

            log.info("Tìm thấy nhân viên id={}", nhanVien.getId());

            Integer trangThaiBan = banAn.getTrangThai();
            LocalDateTime bayGio = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            // LUẬT THOÁNG: Nếu bàn đang báo ĐÃ ĐẶT nhưng nhân viên làm Đặt nhanh (idDatBan == null)
            if (Objects.equals(trangThaiBan, TrangThaiBan.DA_DAT.getValue()) && request.getIdDatBan() == null) {
                // Tìm phiếu đặt thực tế của bàn này
                DatBan dbHienTai = datBanRepository.findTopByBanAns_IdAndTrangThai(
                        Long.valueOf(banAn.getId()),
                        TrangThaiDatBan.DA_XAC_NHAN.getCode()
                );
                if (dbHienTai == null)
                    dbHienTai = datBanRepository.findTopByBanAns_IdAndTrangThai(Long.valueOf(banAn.getId()), TrangThaiDatBan.DA_COC.getCode());

                Integer datTruocToiThieu = thamSoHeThongService.getInt(ThamSoHeThong.DAT_TRUOC_TOI_THIEU.getCode());
                if (datTruocToiThieu == null) datTruocToiThieu = 60;

                // Nếu phiếu đặt còn xa (> datTruocToiThieu phút) -> Ép coi như bàn TRỐNG để cho phép tạo hóa đơn vãng lai
                if (dbHienTai == null || dbHienTai.getThoiGianDat().isAfter(bayGio.plusMinutes(datTruocToiThieu)) || !conHanGiuBan(dbHienTai)) {
                    log.info("[FIX_DAT_NHANH] Bàn {} đặt xa, cho phép đặt nhanh.", banAn.getId());
                    trangThaiBan = TrangThaiBan.TRONG.getValue();
                }
            }

            // 2. LẤY ĐẶT BÀN NẾU CÓ
            DatBan datBan = null;
            boolean isDoiBanKhiCheckIn = false;
            String tenBanCuKhiCheckIn = "";
            String tenBanMoiKhiCheckIn = "";
            if (request.getIdDatBan() != null) {
                datBan = datBanRepository.findDatBanById(request.getIdDatBan())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));

                log.info("Tìm thấy đặt bàn id={}, mã={}, trạng thái={}",
                        datBan.getId(), datBan.getMaDatBan(), datBan.getTrangThai());

                String trangThaiDatBan = datBan.getTrangThai();

                if (!TrangThaiDatBan.DA_XAC_NHAN.getCode().equals(trangThaiDatBan)) {

                    log.error("Đặt bàn chưa đủ điều kiện check-in. trangThaiDatBan={}", trangThaiDatBan);
                    throw new RuntimeException("Chỉ có thể check-in khi đặt bàn ở trạng thái đã xác nhận");
                }

                if (datBan.getBanAns() == null || datBan.getBanAns().isEmpty()) {
                    log.error("Đặt bàn id={} không có danh sách bàn", datBan.getId());
                    throw new RuntimeException("Đặt bàn chưa có bàn nào");
                }

                // CHẶN CHECK-IN KHI CHƯA TỚI GIỜ ĐẶT BÀN
                validateDaToiGioCheckIn(datBan);

                boolean banDuocDat = datBan.getBanAns().stream()
                        .anyMatch(b -> Objects.equals(b.getId(), request.getIdBanAn()));

                if (!banDuocDat) {
                    log.info("Bàn được chọn khác với bàn đã đặt ban đầu. Tiến hành cập nhật bàn mới cho Đặt bàn id={}", datBan.getId());
                    List<Integer> ids = request.getListIdBanAn();
                    if (ids == null || ids.isEmpty()) {
                        ids = List.of(request.getIdBanAn());
                    }
                    List<BanAn> dsBanMoi = quanLyBanRepository.findAllById(ids);

                    isDoiBanKhiCheckIn = true;
                    tenBanCuKhiCheckIn = datBan.getBanAns().stream().map(BanAn::getTenBanAn).collect(Collectors.joining(", "));
                    tenBanMoiKhiCheckIn = dsBanMoi.stream().map(BanAn::getTenBanAn).collect(Collectors.joining(", "));

                    for (BanAn bCu : datBan.getBanAns()) {
                        if (Objects.equals(bCu.getTrangThai(), TrangThaiBan.DA_DAT.getValue())) {
                            bCu.setTrangThai(TrangThaiBan.TRONG.getValue());
                            quanLyBanRepository.save(bCu);
                        }
                    }

                    datBan.getBanAns().clear();
                    datBan.getBanAns().addAll(dsBanMoi);
                    datBan = datBanRepository.saveAndFlush(datBan);
                }

                // KIỂM TRA VA CHẠM: Bàn có đang bị người khác sử dụng không?
                for (BanAn b : datBan.getBanAns()) {
                    if (Objects.equals(b.getTrangThai(), TrangThaiBan.DANG_SU_DUNG.getValue())) {
                        List<Integer> tts = List.of(
                                TrangThaiHoaDon.VUA_TAO.getCode(),
                                TrangThaiHoaDon.CHO_COC.getCode(),
                                TrangThaiHoaDon.DA_COC.getCode(),
                                TrangThaiHoaDon.DA_XAC_NHAN.getCode(),
                                TrangThaiHoaDon.KHACH_DEN.getCode(),
                                TrangThaiHoaDon.DANG_PHUC_VU.getCode(),
                                TrangThaiHoaDon.CHO_THANH_TOAN.getCode()
                        );
                        List<HoaDonBanAn> hdbaList = hoaDonBanAnRepository.findHoaDonDangHoatDongByBan(b.getId(), tts);
                        boolean occupiedByOthers = hdbaList.stream().anyMatch(hdba -> hdba.getHoaDon().getDatBan() == null || !Objects.equals(hdba.getHoaDon().getDatBan().getId(), request.getIdDatBan()));
                        if (occupiedByOthers) {
                            throw new RuntimeException("VA_CHAM_KHACH_DAT: Bàn " + b.getTenBanAn() + " hiện đang có người ngồi. Vui lòng thao tác đổi sang bàn trống khác!");
                        }
                    }
                }
            }

            HoaDon hoaDon;

            // 3. PHÂN NHÁNH THEO TRẠNG THÁI BÀN VÀ ĐẶT BÀN
            if (datBan != null) {
                log.info("=== NHÁNH CHECK-IN THEO ĐẶT BÀN TRƯỚC ===");

                hoaDon = hoaDonRepository.findByDatBan_IdAndTrangThai(datBan.getId(), 0).orElse(null);

                if (hoaDon == null) {
                    hoaDon = hoaDonRepository.findByDatBan(datBan).orElse(null);
                }

                if (hoaDon == null) {
                    hoaDon = HoaDon.builder()
                            .maHoaDon(generateMaHoaDon())
                            .loaiHoaDon(1)
                            .thoiGianTao(DateTimeUtils.now())
                            .thoiGianMo(DateTimeUtils.now())
                            .ngayCapNhat(DateTimeUtils.now())
                            .trangThai(TrangThaiHoaDon.DANG_PHUC_VU.getCode())
                            .ghiChu(request.getGhiChu() != null ? request.getGhiChu().trim() : null)
                            .nhanVien(nhanVien)
                            .datBan(datBan)
                            .khachHang(datBan.getKhachHang())
                            .tongTien(BigDecimal.ZERO)
                            .tongTienMon(BigDecimal.ZERO)
                            .tienSauGiam(BigDecimal.ZERO)
                            .soTienDuocGiam(BigDecimal.ZERO)
                            .phiVanChuyen(BigDecimal.ZERO)
                            .tienThue(BigDecimal.ZERO)
                            .tienCoc(datBan.getTienCoc() != null ? datBan.getTienCoc() : BigDecimal.ZERO)
                            .tongTienDaThanhToan(BigDecimal.ZERO)
                            .build();

                    hoaDon = hoaDonRepository.saveAndFlush(hoaDon);
                    log.info("Bàn đã đặt, chưa có hóa đơn nên đã tạo mới id={}", hoaDon.getId());
                } else {
                    log.info("Bàn đã đặt, tìm thấy hóa đơn id={}", hoaDon.getId());
                }
            }
            // 3.1 BÀN ĐANG SỬ DỤNG -> lấy hóa đơn đang hoạt động (HOẶC tạo mới nếu quick check-in)
            else if (Objects.equals(trangThaiBan, TrangThaiBan.DANG_SU_DUNG.getValue())) {
                log.info("=== NHÁNH BÀN ĐANG SỬ DỤNG ===");

                // FIX: Nếu là check-in nhanh lần 2+ (isQuickCheckIn=true + idDatBan=null) -> tạo hóa đơn mới
                boolean isQuickCheckInLan2 = request.getIsQuickCheckIn() != null && request.getIsQuickCheckIn() && request.getIdDatBan() == null;

                if (isQuickCheckInLan2) {
                    log.info("[QUICK_CHECKIN_LAN_2] Tạo hóa đơn mới cho check-in nhanh lần 2+");

                    // TẠO KHÁCH HÀNG VÃNG LAI MỚI
                    KhachHang khachVangLai = new KhachHang();
                    khachVangLai.setTenKhachHang("Khách vãng lai");
                    khachVangLai.setMatKhau("0000000");
                    khachVangLai.setEmail("khachvanglai" + UUID.randomUUID().toString().substring(0, 8) + "@gmail.com");
                    khachVangLai.setSoDienThoai(null);
                    khachVangLai.setNgaySinh(null);
                    khachVangLai.setTrangThai(1);
                    khachVangLai = khachHangRepository.saveAndFlush(khachVangLai);

                    log.info("Tạo khách hàng vãng lai mới id={}", khachVangLai.getId());

                    // Tạo đặt bàn mới cho check-in nhanh
                    List<Integer> ids = request.getListIdBanAn();
                    if (ids == null || ids.isEmpty()) {
                        ids = List.of(request.getIdBanAn());
                    }
                    List<BanAn> dsBanChon = quanLyBanRepository.findAllById(ids);

                    datBan = DatBan.builder()
                            .khachHang(khachVangLai)
                            .banAns(new ArrayList<>(dsBanChon))
                            .maDatBan(generateMaDatBanQuay())
                            .thoiGianDat(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                            .soNguoiToiDa(request.getSoKhach() != null && request.getSoKhach() > 0 ? request.getSoKhach() : banAn.getSucChuaToiDa())
                            .ghiChu(request.getGhiChu() != null && !request.getGhiChu().trim().isEmpty()
                                    ? request.getGhiChu().trim()
                                    : "Đặt bàn trực tiếp tại quầy (Check-in nhanh)")
                            .hinhThucDat(2)
                            .trangThai(TrangThaiDatBan.DA_XAC_NHAN.getCode())
                            .tienCoc(BigDecimal.ZERO)
                            .phanTramCoc(BigDecimal.ZERO)
                            .hanGiuBan(null)
                            .hanThanhToanCoc(null)
                            .trangThaiCoc("KHONG_CAN_COC")
                            .build();

                    datBan = datBanRepository.saveAndFlush(datBan);
                    log.info("Tạo đặt bàn mới id={} cho check-in nhanh lần 2+", datBan.getId());

                    // Tạo hóa đơn mới
                    hoaDon = HoaDon.builder()
                            .maHoaDon(generateMaHoaDon())
                            .loaiHoaDon(1)
                            .thoiGianTao(DateTimeUtils.now())
                            .thoiGianMo(DateTimeUtils.now())
                            .ngayCapNhat(DateTimeUtils.now())
                            .trangThai(TrangThaiHoaDon.DANG_PHUC_VU.getCode())
                            .ghiChu(request.getGhiChu() != null ? request.getGhiChu().trim() : null)
                            .nhanVien(nhanVien)
                            .khachHang(khachVangLai)
                            .datBan(datBan)
                            .tongTien(BigDecimal.ZERO)
                            .tongTienMon(BigDecimal.ZERO)
                            .tienSauGiam(BigDecimal.ZERO)
                            .soTienDuocGiam(BigDecimal.ZERO)
                            .phiVanChuyen(BigDecimal.ZERO)
                            .tienThue(BigDecimal.ZERO)
                            .tienCoc(BigDecimal.ZERO)
                            .tongTienDaThanhToan(BigDecimal.ZERO)
                            .build();

                    hoaDon = hoaDonRepository.saveAndFlush(hoaDon);
                    log.info("Tạo hóa đơn mới id={} cho check-in nhanh lần 2+", hoaDon.getId());
                } else {
                    // Trường hợp bình thường: lấy hóa đơn đang hoạt động
                    hoaDon = hoaDonBanAnRepository.findTopHoaDonDangHoatDongTheoBan(request.getIdBanAn())
                            .map(HoaDonBanAn::getHoaDon)
                            .orElseThrow(() -> new RuntimeException("Bàn đang sử dụng nhưng không tìm thấy hóa đơn"));

                    log.info("Bàn đang sử dụng, hóa đơn hiện tại id={}", hoaDon.getId());
                }
            }

            // 3.2 BÀN TRỐNG -> tạo khách hàng vãng lai + tạo đặt bàn trực tiếp + tạo hóa đơn
            else if (Objects.equals(trangThaiBan, TrangThaiBan.TRONG.getValue())) {
                log.info("=== NHÁNH BÀN TRỐNG ===");

                // 3.2.1 Tạo khách hàng vãng lai mới
                KhachHang khachVangLai = new KhachHang();
                khachVangLai.setTenKhachHang("Khách vãng lai");
                khachVangLai.setMatKhau("0000000");
                khachVangLai.setEmail("khachvanglai" + UUID.randomUUID().toString().substring(0, 8) + "@gmail.com");
                khachVangLai.setSoDienThoai(null);
                khachVangLai.setNgaySinh(null);
                khachVangLai.setTrangThai(1);
                khachVangLai = khachHangRepository.saveAndFlush(khachVangLai);

                log.info("Tạo khách hàng vãng lai mới id={}, mã={}",
                        khachVangLai.getId(), khachVangLai.getMaKhachHang());

                List<Integer> ids = request.getListIdBanAn();

                if (ids == null || ids.isEmpty()) {
                    ids = List.of(request.getIdBanAn());
                }

                // Trong nhánh xử lý bàn trống (nhân viên đặt nhanh cho khách vãng lai)
                List<BanAn> dsBanChon = quanLyBanRepository.findAllById(ids);

                Integer datTruocToiThieu = thamSoHeThongService.getInt(ThamSoHeThong.DAT_TRUOC_TOI_THIEU.getCode());
                if (datTruocToiThieu == null) datTruocToiThieu = 60;
                final int minMins = datTruocToiThieu;

                // Thêm đoạn kiểm tra này trước khi tạo KhachHang và HoaDon:
                boolean viPhamGioVip = dsBanChon.stream().anyMatch(b -> {
                    // Tìm phiếu đặt sớm nhất của bàn này đang ở trạng thái chờ
                    DatBan dbVip = datBanRepository.findTopByBanAns_IdAndTrangThai(
                            Long.valueOf(b.getId()),
                            TrangThaiDatBan.DA_XAC_NHAN.getCode()
                    );

                    if (dbVip != null) {
                        long minutesUntilVip = java.time.Duration.between(LocalDateTime.now(), dbVip.getThoiGianDat()).toMinutes();
                        // Nếu còn ít hơn minMins phút thì không cho đặt vãng lai đè lên
                        return minutesUntilVip >= 0 && minutesUntilVip <= minMins;
                    }
                    return false;
                });

                if (viPhamGioVip) {
                    throw new RuntimeException("Có bàn bạn chọn sắp đến giờ phục vụ khách đã đặt trước (" + minMins + " phút). Vui lòng đổi bàn khác!");
                }

                if (dsBanChon.isEmpty()) {
                    throw new RuntimeException("Không tìm thấy bàn được chọn");
                }

                // LUẬT THOÁNG: Kiểm tra bàn có thực sự "bận" hay không
                boolean coBanDangBan = dsBanChon.stream().anyMatch(b -> {
                    // 1. Đang có người ngồi ăn -> BẬN
                    if (Objects.equals(b.getTrangThai(), TrangThaiBan.DANG_SU_DUNG.getValue())) return true;

                    // 2. Đã đặt -> Chỉ bận nếu sắp tới giờ (< datTruocToiThieu phút)
                    if (Objects.equals(b.getTrangThai(), TrangThaiBan.DA_DAT.getValue())) {
                        DatBan db = datBanRepository.findTopByBanAns_IdAndTrangThai(Long.valueOf(b.getId()), TrangThaiDatBan.DA_XAC_NHAN.getCode());
                        return db != null && !db.getThoiGianDat().isAfter(bayGio.plusMinutes(minMins));
                    }
                    return false;
                });

                if (coBanDangBan) {
                    throw new RuntimeException("Có bàn đang có khách hoặc sắp tới giờ đặt, không thể đặt nhanh!");
                }

                int tongSucChua = dsBanChon.stream()
                        .mapToInt(b -> b.getSucChuaToiDa() != null ? b.getSucChuaToiDa() : 0)
                        .sum();

                if (tongSucChua < request.getSoKhach()) {
                    throw new RuntimeException("Không đủ chỗ ngồi");
                }

                if (tongSucChua - request.getSoKhach() > 3) {
                    throw new RuntimeException("Bàn quá rộng, số chỗ dư (" + (tongSucChua - request.getSoKhach()) + ") vượt mức cho phép 3 chỗ. Vui lòng chọn bàn khác!");
                }

                // 3.2.2 Tạo đặt bàn trực tiếp
                datBan = DatBan.builder()
                        .khachHang(khachVangLai)
                        .banAns(new ArrayList<>(dsBanChon))
                        .maDatBan(generateMaDatBanQuay())
                        .thoiGianDat(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                        .soNguoiToiDa(
                                request.getSoKhach() != null && request.getSoKhach() > 0
                                        ? request.getSoKhach()
                                        : banAn.getSucChuaToiDa()
                        )
                        .ghiChu(
                                request.getGhiChu() != null && !request.getGhiChu().trim().isEmpty()
                                        ? request.getGhiChu().trim()
                                        : "Đặt bàn trực tiếp tại quầy"
                        )
                        .hinhThucDat(2) // tại quầy
                        .trangThai(TrangThaiDatBan.DA_XAC_NHAN.getCode())
                        .tienCoc(BigDecimal.ZERO)
                        .phanTramCoc(BigDecimal.ZERO)
                        .hanGiuBan(null)
                        .hanThanhToanCoc(null)
                        .trangThaiCoc("KHONG_CAN_COC")
                        .build();

                log.info("Chuẩn bị lưu đặt bàn trực tiếp: maDatBan={}, soNguoiToiDa={}, idBan={}",
                        datBan.getMaDatBan(),
                        datBan.getSoNguoiToiDa(),
                        banAn.getId());

                datBan = datBanRepository.saveAndFlush(datBan);

                log.info("Đã tạo đặt bàn trực tiếp id={}, mã={}", datBan.getId(), datBan.getMaDatBan());

                // 3.2.3 Tạo hóa đơn gắn với đặt bàn vừa tạo
                hoaDon = HoaDon.builder()
                        .maHoaDon(generateMaHoaDon())
                        .loaiHoaDon(1)
                        .thoiGianTao(DateTimeUtils.now())
                        .thoiGianMo(DateTimeUtils.now())
                        .ngayCapNhat(DateTimeUtils.now())
                        .trangThai(TrangThaiHoaDon.DANG_PHUC_VU.getCode())
                        .ghiChu(request.getGhiChu() != null ? request.getGhiChu().trim() : null)
                        .nhanVien(nhanVien)
                        .khachHang(khachVangLai)
                        .datBan(datBan)
                        .tongTien(BigDecimal.ZERO)
                        .tongTienMon(BigDecimal.ZERO)
                        .tienSauGiam(BigDecimal.ZERO)
                        .soTienDuocGiam(BigDecimal.ZERO)
                        .phiVanChuyen(BigDecimal.ZERO)
                        .tienThue(BigDecimal.ZERO)
                        .tienCoc(BigDecimal.ZERO)
                        .tongTienDaThanhToan(BigDecimal.ZERO)
                        .build();

                log.info("Chuẩn bị lưu hóa đơn mới: maHoaDon={}, idDatBan={}, idKhachHang={}, idNhanVien={}",
                        hoaDon.getMaHoaDon(),
                        datBan.getId(),
                        khachVangLai.getId(),
                        nhanVien.getId());

                hoaDon = hoaDonRepository.saveAndFlush(hoaDon);

                log.info("Bàn trống, đã tạo hóa đơn id={} gắn với đặt bàn id={}", hoaDon.getId(), datBan.getId());
            }

            else {
                throw new RuntimeException("Bàn không ở trạng thái hợp lệ để check-in");
            }

            // 4. XÁC ĐỊNH DANH SÁCH BÀN CHECK-IN
            List<BanAn> dsBanCanCheckIn;

            if (datBan != null && datBan.getBanAns() != null && !datBan.getBanAns().isEmpty()) {
                dsBanCanCheckIn = datBan.getBanAns();
            } else {
                dsBanCanCheckIn = List.of(banAn);
            }

            log.info("Danh sách bàn cần check-in: {}",
                    dsBanCanCheckIn.stream().map(BanAn::getId).collect(Collectors.toList()));

            // 5. CẬP NHẬT TRẠNG THÁI BÀN
            for (BanAn ban : dsBanCanCheckIn) {
                if (!Objects.equals(ban.getTrangThai(), TrangThaiBan.DA_DAT.getValue())
                        && !Objects.equals(ban.getTrangThai(), TrangThaiBan.TRONG.getValue())
                        && !Objects.equals(ban.getTrangThai(), TrangThaiBan.DANG_SU_DUNG.getValue())) {
                    log.warn("Bàn id={} đang ở trạng thái={} vẫn sẽ cập nhật sang ĐANG_SỬ_DỤNG",
                            ban.getId(), ban.getTrangThai());
                }

                ban.setTrangThai(TrangThaiBan.DANG_SU_DUNG.getValue());
                ban.setNgayCapNhat(DateTimeUtils.now());
                quanLyBanRepository.save(ban);

                log.info("Đã cập nhật bàn id={} sang DANG_SU_DUNG", ban.getId());
            }

            // 6. CẬP NHẬT HÓA ĐƠN
            hoaDon.setNhanVien(nhanVien);
            hoaDon.setTrangThai(TrangThaiHoaDon.DANG_PHUC_VU.getCode());
            hoaDon.setThoiGianMo(hoaDon.getThoiGianMo() == null ? DateTimeUtils.now() : hoaDon.getThoiGianMo());
            hoaDon.setNgayCapNhat(DateTimeUtils.now());

            if (request.getGhiChu() != null && !request.getGhiChu().trim().isEmpty()) {
                hoaDon.setGhiChu(request.getGhiChu().trim());
            }

            hoaDon = hoaDonRepository.saveAndFlush(hoaDon);
            log.info("Đã cập nhật hóa đơn id={} sang trạng thái={}", hoaDon.getId(), hoaDon.getTrangThai());

            // 7. CẬP NHẬT LIÊN KẾT HÓA ĐƠN - BÀN
            hoaDonBanAnRepository.deleteByHoaDon_Id(hoaDon.getId());
            log.info("Đã xóa liên kết bàn cũ của hóa đơn id={}", hoaDon.getId());

            int tongKhachConLai = request.getSoKhach();

            for (int i = 0; i < dsBanCanCheckIn.size(); i++) {
                BanAn ban = dsBanCanCheckIn.get(i);
                int sucChua = ban.getSucChuaToiDa() != null ? ban.getSucChuaToiDa() : 0;

                int khachNgoiBanNay = 0;
                if (tongKhachConLai > 0) {
                    if (tongKhachConLai >= sucChua) {
                        khachNgoiBanNay = sucChua;
                        tongKhachConLai -= sucChua;
                    } else {
                        khachNgoiBanNay = tongKhachConLai;
                        tongKhachConLai = 0;
                    }
                }

                // Bàn cuối cùng sẽ gánh số khách dư nếu có
                if (i == dsBanCanCheckIn.size() - 1 && tongKhachConLai > 0) {
                    khachNgoiBanNay += tongKhachConLai;
                    tongKhachConLai = 0;
                }

                HoaDonBanAn hdba = HoaDonBanAn.builder()
                        .hoaDon(hoaDon)
                        .banAn(ban)
                        .soKhach(khachNgoiBanNay)
                        .build();
                hoaDonBanAnRepository.save(hdba);
            }

            // 8. CẬP NHẬT ĐẶT BÀN -> DANG_PHUC_VU
            if (datBan != null) {
                datBan.setTrangThai(TrangThaiDatBan.DANG_PHUC_VU.getCode());
                datBan.setNgayCapNhat(DateTimeUtils.now());
                datBanRepository.save(datBan);
                log.info("Đã cập nhật đặt bàn id={} sang DANG_PHUC_VU", datBan.getId());
            }

            // 9. LƯU LỊCH SỬ HÓA ĐƠN
            lichSuHoaDonRepository.save(
                    LichSuHoaDon.builder()
                            .hoaDon(hoaDon)
                            .thoiGian(DateTimeUtils.now())
                            .ngayTao(DateTimeUtils.now())
                            .ngayCapNhat(DateTimeUtils.now())
                            .hanhDong("CHECK_IN")
                            .trangThai(1)
                            .ghiChu(request.getGhiChu() != null && !request.getGhiChu().trim().isEmpty()
                                    ? request.getGhiChu().trim()
                                    : "Khách đã check-in")
                            .nhanVien(nhanVien)
                            .build()
            );
            log.info("Đã lưu lịch sử hóa đơn cho hóa đơn id={}", hoaDon.getId());

            if (isDoiBanKhiCheckIn) {
                lichSuHoaDonRepository.save(
                        LichSuHoaDon.builder()
                                .hoaDon(hoaDon)
                                .thoiGian(DateTimeUtils.now())
                                .ngayTao(DateTimeUtils.now())
                                .ngayCapNhat(DateTimeUtils.now())
                                .hanhDong("DOI_BAN")
                                .trangThai(1)
                                .ghiChu("Khách đổi từ bàn " + tenBanCuKhiCheckIn + " sang bàn " + tenBanMoiKhiCheckIn + " khi check-in do bàn đặt đã có người sử dụng")
                                .nhanVien(nhanVien)
                                .build()
                );
                log.info("Đã lưu lịch sử đổi bàn khi check-in cho hóa đơn id={}", hoaDon.getId());
            }

            // 10. RESPONSE
            return CheckInResponse.builder()
                    .idBanAn(banAn.getId())
                    .maBanAn(banAn.getMaBanAn())
                    .tenBanAn(banAn.getTenBanAn())
                    .sucChuaToiDa(banAn.getSucChuaToiDa())
                    .trangThaiBan(TrangThaiBan.DANG_SU_DUNG.getValue())
                    .khuVucId(banAn.getKhuVuc() != null ? banAn.getKhuVuc().getId() : null)
                    .tenKhuVuc(banAn.getKhuVuc() != null ? banAn.getKhuVuc().getTenKhuVuc() : null)

                    .idDatBan(datBan != null ? datBan.getId() : null)
                    .maDatBan(datBan != null ? datBan.getMaDatBan() : null)
                    .tenKhachHang(datBan != null && datBan.getKhachHang() != null
                            ? datBan.getKhachHang().getTenKhachHang()
                            : "Khách hàng vãng lai")
                    .soDienThoai(datBan != null && datBan.getKhachHang() != null
                            ? datBan.getKhachHang().getSoDienThoai()
                            : null)
                    .hinhThucDat(datBan != null ? datBan.getHinhThucDat() : null)
                    .thoiGianDat(datBan != null ? datBan.getThoiGianDat() : null)
                    .soNguoiToiDa(datBan != null ? datBan.getSoNguoiToiDa() : request.getSoKhach())
                    .trangThaiDatBan(datBan != null ? datBan.getTrangThai() : null)
                    .ghiChuDatBan(datBan != null ? datBan.getGhiChu() : request.getGhiChu())

                    .idHoaDon(hoaDon.getId())
                    .maHoaDon(hoaDon.getMaHoaDon())
                    .trangThaiHoaDon(hoaDon.getTrangThai())
                    .message("Check-in thành công")
                    .build();

        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }

            log.error("LỖI CHECK-IN: {}", e.getMessage(), e);
            log.error("ROOT CAUSE CHECK-IN: {}", root.getMessage(), root);

            String message = root.getMessage() != null
                    ? root.getMessage()
                    : (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());

            throw new RuntimeException("Lỗi check-in: " + message, e);
        }
    }

    private BigDecimal tinhGiaSauGiam(SanPham sp) {
        BigDecimal giaGoc = sp.getGia() == null ? BigDecimal.ZERO : sp.getGia();

        List<DotGiamGiaSanPham> ds =
                dotGiamGiaSanPhamRepository.findDangApDung(
                        sp.getId(),
                        DateTimeUtils.now()
                );

        if (ds == null || ds.isEmpty()) {
            return giaGoc;
        }

        DotGiamGia dot = ds.get(0).getDotGiamGia();

        BigDecimal soTienGiam = BigDecimal.ZERO;

        // Chỉ có 1 loại là giảm %
        if (dot.getGiaTriGiam() != null && dot.getGiaTriGiam().compareTo(BigDecimal.ZERO) > 0) {
            soTienGiam = giaGoc
                    .multiply(dot.getGiaTriGiam())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        }

        return giaGoc.subtract(soTienGiam).max(BigDecimal.ZERO);
    }

    private SanPhamResponse mapSanPhamCheckIn(SanPham sp) {
        BigDecimal giaGoc = sp.getGia() == null ? BigDecimal.ZERO : sp.getGia();
        BigDecimal giaSauGiam = tinhGiaSauGiam(sp);
        BigDecimal soTienGiam = giaGoc.subtract(giaSauGiam).max(BigDecimal.ZERO);

        Integer phanTramGiam = 0;
        if (giaGoc.compareTo(BigDecimal.ZERO) > 0 && soTienGiam.compareTo(BigDecimal.ZERO) > 0) {
            phanTramGiam = soTienGiam
                    .multiply(BigDecimal.valueOf(100))
                    .divide(giaGoc, 0, java.math.RoundingMode.HALF_UP)
                    .intValue();
        }

        return SanPhamResponse.builder()
                .id(sp.getId())
                .maSanPham(sp.getMaSanPham())
                .tenSanPham(sp.getTenSanPham())
                .gia(giaGoc)
                .giaSauGiam(giaSauGiam)
                .soTienGiam(soTienGiam)
                .phanTramGiam(phanTramGiam)
                .moTa(sp.getMoTa())
                .hinhAnh(sp.getHinhAnh())
                .trangThaiXoa(sp.getTrangThaiXoa())
                .laSet(sp.getLaSet())
                .tenNuocLau(sp.getNuocLau() != null ? sp.getNuocLau().getTenNuocLau() : null)
                .idNuocLau(sp.getNuocLau() != null ? sp.getNuocLau().getId() : null)
                .build();
    }

    @Override
    public List<SanPhamResponse> getAllMonLe() {
        List<SanPham> danhSach = sanPhamRepository.findAllMonLe();

        return danhSach.stream()
                .map(this::mapSanPhamCheckIn)
                .toList();
    }

    @Override
    public List<SanPhamResponse> getAllSetLau() {
        List<SanPham> danhSach = sanPhamRepository.findAllSetLau();

        return danhSach.stream()
                .map(this::mapSanPhamCheckIn)
                .toList();
    }

    @Override
    public List<SanPhamResponse> getAllThucDon() {
        List<SanPhamResponse> monLe = getAllMonLe();
        List<SanPhamResponse> setLau = getAllSetLau();

        List<SanPhamResponse> all = new ArrayList<>();
        all.addAll(setLau);
        all.addAll(monLe);

        all.sort(Comparator.comparing(SanPhamResponse::getId).reversed());
        return all;
    }

    @Override
    public List<SanPhamResponse> searchMonLe(String keyword, Long idDanhMuc, BigDecimal giaMin, BigDecimal giaMax) {
        List<SanPham> danhSach = sanPhamRepository.searchMonLe(keyword, idDanhMuc, giaMin, giaMax);

        return danhSach.stream()
                .map(this::mapSanPhamCheckIn)
                .toList();
    }

    @Override
    public List<SanPhamResponse> searchSetLau(String keyword, Long idNuocLau) {
        List<SanPham> danhSach = sanPhamRepository.searchSetLau(keyword, idNuocLau);

        return danhSach.stream()
                .map(this::mapSanPhamCheckIn)
                .toList();
    }

    private void capNhatTongTienHoaDon(Long idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        List<HoaDonChiTiet> dsChiTiet = hoaDonChiTietRepository.findAllByHoaDon_Id(idHoaDon);

        // chỉ tính món đã lên
        BigDecimal tongTienMon = dsChiTiet.stream()
                .filter(ct -> ct.getTrangThai() != null && ct.getTrangThai() == 1)
                .map(ct -> {
                    if (ct.getThanhTien() != null) {
                        return ct.getThanhTien();
                    } else if (ct.getDonGia() != null && ct.getSoLuong() != null) {
                        return ct.getDonGia().multiply(BigDecimal.valueOf(ct.getSoLuong()));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal soTienDuocGiam = hoaDon.getSoTienDuocGiam() != null
                ? hoaDon.getSoTienDuocGiam()
                : BigDecimal.ZERO;
                
        // Tự động tính lại số tiền giảm nếu hóa đơn đang áp dụng phiếu giảm giá
        if (hoaDon.getPhieuGiamGia() != null) {
            soTienDuocGiam = tinhTienGiam(hoaDon.getPhieuGiamGia(), tongTienMon);
            hoaDon.setSoTienDuocGiam(soTienDuocGiam);
        }

        BigDecimal tienCoc = hoaDon.getTienCoc() != null
                ? hoaDon.getTienCoc()
                : BigDecimal.ZERO;


        BigDecimal phiThuKhac = hoaDon.getPhiVanChuyen() != null
                ? hoaDon.getPhiVanChuyen()
                : BigDecimal.ZERO;

        BigDecimal tongTien = tongTienMon.add(phiThuKhac);

        BigDecimal tienSauGiam = tongTienMon.subtract(soTienDuocGiam);
        if (tienSauGiam.compareTo(BigDecimal.ZERO) < 0) {
            tienSauGiam = BigDecimal.ZERO;
        }
        
        BigDecimal tienDaThanhToan = hoaDon.getTongTienDaThanhToan() != null ? hoaDon.getTongTienDaThanhToan() : BigDecimal.ZERO;
        BigDecimal tienConLai = tongTien.subtract(soTienDuocGiam).subtract(tienCoc).subtract(tienDaThanhToan);
        if (tienConLai.compareTo(BigDecimal.ZERO) < 0) {
            tienConLai = BigDecimal.ZERO;
        }

        hoaDon.setTongTienMon(tongTienMon);
        hoaDon.setTienSauGiam(tienSauGiam);
        hoaDon.setTienConLai(tienConLai);
        hoaDon.setTongTien(tongTien);
        hoaDon.setNgayCapNhat(DateTimeUtils.now());
        log.info("[TONG_TIEN] idHoaDon={} | tongTienMon={} | giam={} | tienSauGiam={} | tongTien={}",
                hoaDon.getId(), tongTienMon, soTienDuocGiam, tienSauGiam, tongTien);
        hoaDonRepository.save(hoaDon);
    }

    private String taoPrefixTenBan(String tenBan, Long idBanAn) {
        if (idBanAn != null) {
            BanAn banAn = quanLyBanRepository.findById(Math.toIntExact(idBanAn)).orElse(null);
            if (banAn != null) {
                return "Bàn " + banAn.getTenBanAn() + " - ";
            }
        }
        if (tenBan != null && !tenBan.trim().isEmpty() && !"null".equalsIgnoreCase(tenBan.trim())) {
            String cleanBan = tenBan.trim();
            if (cleanBan.toLowerCase().startsWith("bàn ")) {
                cleanBan = cleanBan.substring(4).trim();
            }
            return "Bàn " + cleanBan + " - ";
        }
        return "";
    }

    private String taoPrefixTenBan(String tenBan, Long idHoaDon, boolean useAllTables) {
        if (tenBan != null && !tenBan.trim().isEmpty() && !"null".equalsIgnoreCase(tenBan.trim())) {
            String cleanBan = tenBan.trim();
            if (cleanBan.toLowerCase().startsWith("bàn ")) {
                cleanBan = cleanBan.substring(4).trim();
            }
            return "Bàn " + cleanBan + " - ";
        }
        if (useAllTables && idHoaDon != null) {
            List<String> listTenBan = layListTenBanTheoHoaDon(idHoaDon);
            if (listTenBan != null && !listTenBan.isEmpty()) {
                return "Bàn " + String.join(", ", listTenBan) + " - ";
            }
        }
        return "";
    }

    @Override
    @Transactional
    public HoaDonChiTietResponse themMonVaoHoaDon(Long idHoaDon, Long idSanPham, Integer soLuong, String tenBan, Long idBanAn) {
        if (idHoaDon == null) {
            throw new RuntimeException("Thiếu id hóa đơn");
        }
        if (idSanPham == null) {
            throw new RuntimeException("Thiếu id sản phẩm");
        }
        if (soLuong == null || soLuong <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        SanPham sanPham = sanPhamRepository.findById(idSanPham)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        BigDecimal donGia = tinhGiaSauGiam(sanPham);

        Optional<HoaDonChiTiet> optional =
                hoaDonChiTietRepository.findByHoaDon_IdAndSanPham_IdAndTrangThai(idHoaDon, idSanPham, 0);

        HoaDonChiTiet hoaDonChiTiet;

        if (optional.isPresent()) {
            hoaDonChiTiet = optional.get();
            hoaDonChiTiet.setSoLuong(hoaDonChiTiet.getSoLuong() + soLuong);
            hoaDonChiTiet.setThanhTien(
                    hoaDonChiTiet.getDonGia().multiply(BigDecimal.valueOf(hoaDonChiTiet.getSoLuong()))
            );
            hoaDonChiTiet.setNgayCapNhat(DateTimeUtils.now());
        } else {
            hoaDonChiTiet = HoaDonChiTiet.builder()
                    .hoaDon(hoaDon)
                    .sanPham(sanPham)
                    .soLuong(soLuong)
                    .donGia(donGia)
                    .thanhTien(donGia.multiply(BigDecimal.valueOf(soLuong)))
                    .ngayTao(DateTimeUtils.now())
                    .ngayCapNhat(DateTimeUtils.now())
                    .trangThai(0)
                    .build();
        }

        HoaDonChiTiet saved = hoaDonChiTietRepository.save(hoaDonChiTiet);

        hoaDon.setTrangThai(TrangThaiHoaDon.DANG_PHUC_VU.getCode());
        hoaDon.setNgayCapNhat(DateTimeUtils.now());
        hoaDonRepository.save(hoaDon);

        String prefix = taoPrefixTenBan(tenBan, idBanAn);

        lichSuHoaDonRepository.save(
                LichSuHoaDon.builder()
                        .hoaDon(hoaDon)
                        .thoiGian(DateTimeUtils.now())
                        .ngayTao(DateTimeUtils.now())
                        .ngayCapNhat(DateTimeUtils.now())
                        .hanhDong("GOI_MON")
                        .trangThai(1)
                        .ghiChu(prefix + "Đã gọi món: " + sanPham.getTenSanPham())
                        .nhanVien(hoaDon.getNhanVien())
                        .build()
        );

        capNhatTongTienHoaDon(idHoaDon);

        webSocketService.sendMonDaChonUpdate();

        String tenBanAn = tenBan;
        if (idBanAn != null) {
            BanAn banAn = quanLyBanRepository.findById(Math.toIntExact(idBanAn)).orElse(null);
            if (banAn != null) {
                tenBanAn = banAn.getTenBanAn();
            }
        }

        return HoaDonChiTietResponse.builder()
                .id(saved.getId())
                .soLuong(saved.getSoLuong())
                .donGia(saved.getDonGia())
                .thanhTien(saved.getThanhTien())
                .ngayTao(saved.getNgayTao())
                .ngayCapNhat(saved.getNgayCapNhat())
                .trangThai(saved.getTrangThai())
                .idSanPham(saved.getSanPham() != null ? saved.getSanPham().getId() : null)
                .tenSanPham(saved.getSanPham() != null ? saved.getSanPham().getTenSanPham() : null)
                .tenDanhMuc(
                        saved.getSanPham() != null && saved.getSanPham().getDanhMuc() != null
                                ? saved.getSanPham().getDanhMuc().getTenDanhMuc()
                                : null
                )
                .hinhAnh(saved.getSanPham() != null ? saved.getSanPham().getHinhAnh() : null)
                .idHoaDon(saved.getHoaDon() != null ? saved.getHoaDon().getId() : null)
                .maHoaDon(saved.getHoaDon() != null ? saved.getHoaDon().getMaHoaDon() : null)
                .tenBanGoi(tenBanAn)
                .build();
    }

    @Override
    public List<HoaDonChiTietResponse> getMonDaChonTheoHoaDon(Long idHoaDon) {
        if (idHoaDon == null) {
            throw new RuntimeException("Thiếu id hóa đơn");
        }

        List<HoaDonChiTiet> ds = hoaDonChiTietRepository.findMonDaChonByHoaDonId(idHoaDon);
        List<LichSuHoaDon> dsLichSu = lichSuHoaDonRepository.findByHoaDon_IdOrderByThoiGianDesc(idHoaDon);

        return ds.stream().map(ct -> {
            String tenBanGoi = null;
            if (ct.getSanPham() != null) {
                String tenSanPham = ct.getSanPham().getTenSanPham();
                for (LichSuHoaDon ls : dsLichSu) {
                    if (ls.getGhiChu() != null && ls.getGhiChu().contains(tenSanPham) && "GOI_MON".equals(ls.getHanhDong())) {
                        String ghiChu = ls.getGhiChu();
                        int separatorIdx = ghiChu.indexOf(" - Đã gọi món");
                        if (separatorIdx > 0) {
                            tenBanGoi = ghiChu.substring(0, separatorIdx).trim();
                        } else {
                            int colonIdx = ghiChu.indexOf(":");
                            if (colonIdx > 0) {
                                String temp = ghiChu.substring(0, colonIdx).trim();
                                if(temp.contains(" - ")) {
                                    tenBanGoi = temp.substring(0, temp.indexOf(" - ")).trim();
                                } else {
                                    tenBanGoi = temp;
                                }
                            }
                        }
                        break;
                    }
                }
            }

            return HoaDonChiTietResponse.builder()
                    .id(ct.getId())
                    .soLuong(ct.getSoLuong())
                    .donGia(ct.getDonGia())
                    .thanhTien(ct.getThanhTien())
                    .ngayTao(ct.getNgayTao())
                    .ngayCapNhat(ct.getNgayCapNhat())
                    .trangThai(ct.getTrangThai())
                    .idSanPham(ct.getSanPham() != null ? ct.getSanPham().getId() : null)
                    .tenSanPham(ct.getSanPham() != null ? ct.getSanPham().getTenSanPham() : null)
                    .tenDanhMuc(
                            ct.getSanPham() != null && ct.getSanPham().getDanhMuc() != null
                                    ? ct.getSanPham().getDanhMuc().getTenDanhMuc()
                                    : null
                    )
                    .hinhAnh(ct.getSanPham() != null ? ct.getSanPham().getHinhAnh() : null)
                    .idHoaDon(ct.getHoaDon() != null ? ct.getHoaDon().getId() : null)
                    .maHoaDon(ct.getHoaDon() != null ? ct.getHoaDon().getMaHoaDon() : null)
                    .tenBanGoi(tenBanGoi)
                    .build();
        }).toList();
    }

    @Override
    public List<LichSuHoaDonResponse> getLichSuHoaDon(Long idHoaDon) {
        if (idHoaDon == null) {
            throw new RuntimeException("Thiếu id hóa đơn");
        }

        hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));


        List<LichSuHoaDon> dsLichSu = lichSuHoaDonRepository.findByHoaDon_IdOrderByThoiGianDesc(idHoaDon);

        return dsLichSu.stream().map(ls -> LichSuHoaDonResponse.builder()
                        .id(ls.getId())
                        .idHoaDon(ls.getHoaDon() != null ? ls.getHoaDon().getId() : null)
                        .ngayTao(ls.getNgayTao())
                        .ngayCapNhat(ls.getNgayCapNhat())
                        .thoiGian(ls.getThoiGian())
                        .hanhDong(ls.getHanhDong())
                        .trangThai(ls.getTrangThai())
                        .ghiChu(ls.getGhiChu())
                        .tenNhanVien(ls.getNhanVien() != null ? ls.getNhanVien().getTenNhanVien() : null)
                        .build())
                .toList();
    }


    //28/03
    private HoaDonChiTietResponse mapToHoaDonChiTietResponse(HoaDonChiTiet ct) {
        return HoaDonChiTietResponse.builder()
                .id(ct.getId())
                .soLuong(ct.getSoLuong())
                .donGia(ct.getDonGia())
                .thanhTien(ct.getThanhTien())
                .ngayTao(ct.getNgayTao())
                .ngayCapNhat(ct.getNgayCapNhat())
                .trangThai(ct.getTrangThai())
                .idSanPham(ct.getSanPham() != null ? ct.getSanPham().getId() : null)
                .tenSanPham(ct.getSanPham() != null ? ct.getSanPham().getTenSanPham() : null)
                .tenDanhMuc(
                        ct.getSanPham() != null && ct.getSanPham().getDanhMuc() != null
                                ? ct.getSanPham().getDanhMuc().getTenDanhMuc()
                                : null
                )
                .hinhAnh(ct.getSanPham() != null ? ct.getSanPham().getHinhAnh() : null)
                .idHoaDon(ct.getHoaDon() != null ? ct.getHoaDon().getId() : null)
                .maHoaDon(ct.getHoaDon() != null ? ct.getHoaDon().getMaHoaDon() : null)
                .build();
    }

    @Override
    @Transactional
    public HoaDonChiTietResponse daLenMon(Long idHoaDonChiTiet, String tenBan) {
        if (idHoaDonChiTiet == null) {
            throw new RuntimeException("Thiếu id hóa đơn chi tiết");
        }

        HoaDonChiTiet ct = hoaDonChiTietRepository.findChiTietById(idHoaDonChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món trong hóa đơn"));

        log.info("[LEN_MON] idHoaDonChiTiet={} | trangThaiCu={}", idHoaDonChiTiet, ct.getTrangThai());
        if (ct.getTrangThai() != null && ct.getTrangThai() == 1) {
            return mapToHoaDonChiTietResponse(ct);
        }

        int soDongCapNhat = hoaDonChiTietRepository.daLenMon(idHoaDonChiTiet, DateTimeUtils.now());
        if (soDongCapNhat <= 0) {
            throw new RuntimeException("Món đã được lên trước đó hoặc cập nhật thất bại");
        }

        HoaDonChiTiet updated = hoaDonChiTietRepository.findChiTietById(idHoaDonChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món trong hóa đơn sau cập nhật"));

        HoaDon hoaDon = updated.getHoaDon();
        if (hoaDon != null) {
            hoaDon.setTrangThai(TrangThaiHoaDon.DANG_PHUC_VU.getCode());
            hoaDon.setNgayCapNhat(DateTimeUtils.now());
            hoaDonRepository.save(hoaDon);

            // cập nhật lại tổng tiền sau khi món vừa được lên
            capNhatTongTienHoaDon(hoaDon.getId());

            String prefix = taoPrefixTenBan(tenBan, hoaDon.getId(), true);
            String ghiChuLichSu = prefix + "Đã lên món: " +
                    (updated.getSanPham() != null ? updated.getSanPham().getTenSanPham() : "");
                    
            if (ghiChuLichSu.length() > 255) {
                ghiChuLichSu = ghiChuLichSu.substring(0, 252) + "...";
            }

            lichSuHoaDonRepository.save(
                    LichSuHoaDon.builder()
                            .hoaDon(hoaDon)
                            .thoiGian(DateTimeUtils.now())
                            .ngayTao(DateTimeUtils.now())
                            .ngayCapNhat(DateTimeUtils.now())
                            .hanhDong("LEN_MON")
                            .trangThai(1)
                            .ghiChu(ghiChuLichSu)
                            .nhanVien(hoaDon.getNhanVien())
                            .build()
            );

            boolean conMonCho = hoaDonChiTietRepository.existsByHoaDon_IdAndTrangThai(hoaDon.getId(), 0);
            if (!conMonCho) {
                hoaDon.setTrangThai(TrangThaiHoaDon.CHO_THANH_TOAN.getCode());
                hoaDon.setNgayCapNhat(DateTimeUtils.now());
                hoaDonRepository.save(hoaDon);

                // cập nhật lại tiền lần cuối sau khi tất cả món đã lên
                capNhatTongTienHoaDon(hoaDon.getId());
            }

            webSocketService.sendMonDaChonUpdate();
        }

        return mapToHoaDonChiTietResponse(updated);
    }

    @Override
    @Transactional
    public List<HoaDonChiTietResponse> daLenTatCa(Long idHoaDon, String tenBan) {
        if (idHoaDon == null) {
            throw new RuntimeException("Thiếu id hóa đơn");
        }

        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        List<String> dsTenMonCho = hoaDonChiTietRepository.findAllByHoaDon_Id(idHoaDon).stream()
                .filter(ct -> ct.getTrangThai() != null && ct.getTrangThai() == 0)
                .map(ct -> ct.getSanPham() != null ? ct.getSanPham().getTenSanPham() : "Món không xác định")
                .toList();
        String tenCacMon = String.join(", ", dsTenMonCho);

        hoaDonChiTietRepository.daLenTatCa(idHoaDon, DateTimeUtils.now());

        // cập nhật tổng tiền sau khi tất cả món đã lên
        capNhatTongTienHoaDon(idHoaDon);

        hoaDon.setTrangThai(TrangThaiHoaDon.CHO_THANH_TOAN.getCode());
        hoaDon.setNgayCapNhat(DateTimeUtils.now());
        hoaDonRepository.save(hoaDon);

        String prefix = taoPrefixTenBan(tenBan, idHoaDon, true);
        String ghiChuLichSu = prefix + "Đã lên tất cả món" + (!tenCacMon.isEmpty() ? ": " + tenCacMon : "");
        
        if (ghiChuLichSu.length() > 255) {
            ghiChuLichSu = ghiChuLichSu.substring(0, 252) + "...";
        }

        lichSuHoaDonRepository.save(
                LichSuHoaDon.builder()
                        .hoaDon(hoaDon)
                        .thoiGian(DateTimeUtils.now())
                        .ngayTao(DateTimeUtils.now())
                        .ngayCapNhat(DateTimeUtils.now())
                        .hanhDong("LEN_TAT_CA")
                        .trangThai(1)
                        .ghiChu(ghiChuLichSu)
                        .nhanVien(hoaDon.getNhanVien())
                        .build()
        );

        webSocketService.sendMonDaChonUpdate();

        return getMonDaChonTheoHoaDon(idHoaDon);
    }

    @Override
    @Transactional
    public void xoaMonKhoiHoaDon(Long idHoaDonChiTiet, String tenBan) {
        if (idHoaDonChiTiet == null) {
            throw new RuntimeException("Thiếu id hóa đơn chi tiết");
        }

        HoaDonChiTiet ct = hoaDonChiTietRepository.findChiTietById(idHoaDonChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món trong hóa đơn"));

        HoaDon hoaDon = ct.getHoaDon();
        Long idHoaDon = hoaDon != null ? hoaDon.getId() : null;
        String tenMon = ct.getSanPham() != null ? ct.getSanPham().getTenSanPham() : "";

        String prefix = taoPrefixTenBan(tenBan, idHoaDon, true);
        String ghiChuLichSu = "";

        if (ct.getSoLuong() != null && ct.getSoLuong() > 1) {
            ct.setSoLuong(ct.getSoLuong() - 1);
            ct.setThanhTien(ct.getDonGia().multiply(BigDecimal.valueOf(ct.getSoLuong())));
            ct.setNgayCapNhat(DateTimeUtils.now());
            hoaDonChiTietRepository.save(ct);
            ghiChuLichSu = prefix + "Đã giảm số lượng món: " + tenMon;
        } else {
            int soDongBiXoa = hoaDonChiTietRepository.xoaMonKhoiHoaDon(idHoaDonChiTiet);
            if (soDongBiXoa <= 0) {
                throw new RuntimeException("Xóa món thất bại");
            }
            ghiChuLichSu = prefix + "Đã xóa món: " + tenMon;
        }

        if (ghiChuLichSu.length() > 255) {
            ghiChuLichSu = ghiChuLichSu.substring(0, 252) + "...";
        }

        if (idHoaDon != null) {
            capNhatTongTienHoaDon(idHoaDon);
        }

        if (hoaDon != null) {
            boolean conMonCho = hoaDonChiTietRepository.existsByHoaDon_IdAndTrangThai(hoaDon.getId(), 0);

            hoaDon.setNgayCapNhat(DateTimeUtils.now());
            if (conMonCho) {
                hoaDon.setTrangThai(TrangThaiHoaDon.DANG_PHUC_VU.getCode());
            } else {
                hoaDon.setTrangThai(TrangThaiHoaDon.CHO_THANH_TOAN.getCode());
            }
            hoaDonRepository.save(hoaDon);

            lichSuHoaDonRepository.save(
                    LichSuHoaDon.builder()
                            .hoaDon(hoaDon)
                            .thoiGian(DateTimeUtils.now())
                            .ngayTao(DateTimeUtils.now())
                            .ngayCapNhat(DateTimeUtils.now())
                            .hanhDong("XOA_MON")
                            .trangThai(1)
                            .ghiChu(ghiChuLichSu)
                            .nhanVien(hoaDon.getNhanVien())
                            .build()
            );

            webSocketService.sendMonDaChonUpdate();
        }
    }


    private BigDecimal tinhTienGiam(PhieuGiamGia pgg, BigDecimal tongTienMon) {
        if (pgg == null || tongTienMon == null) return BigDecimal.ZERO;

        BigDecimal giaTriGiam = pgg.getGiaTriGiam() != null ? pgg.getGiaTriGiam() : BigDecimal.ZERO;

        // phuongThucGiamGia = 1 => %
        if (Objects.equals(pgg.getPhuongThucGiamGia(), 1)) {
            BigDecimal tienGiam = tongTienMon
                    .multiply(giaTriGiam)
                    .divide(BigDecimal.valueOf(100));

            if (pgg.getGiamToiDa() != null && pgg.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
                return tienGiam.min(pgg.getGiamToiDa());
            }
            return tienGiam;
        }

        // phuongThucGiamGia = 2 => tiền mặt
        return giaTriGiam.min(tongTienMon);
    }

    private boolean isPhieuHopLe(PhieuGiamGia p, BigDecimal tongTienMon, LocalDate today) {
        if (p == null) return false;

        if (Boolean.TRUE.equals(p.getTrangThaiXoa())) return false;

        if (p.getNgayBatDau() == null || p.getNgayBatDau().isAfter(today)) return false;

        if (p.getNgayKetThuc() == null || p.getNgayKetThuc().isBefore(today)) return false;

        if (p.getSoLuong() == null || p.getSoLuong() <= 0) return false;

        return p.getDieuKienSuDung() == null || tongTienMon.compareTo(p.getDieuKienSuDung()) >= 0;
    }

    @Override
    public void tuDongDonBan(BanAn ban, HoaDon hoaDon) {
        ban.setTrangThai(TrangThaiBan.DANG_DON.getValue());
        ban.setNgayCapNhat(DateTimeUtils.now());
        quanLyBanRepository.save(ban);
    }

    private BigDecimal tinhTienGiamDuKienChoHoaDon(HoaDon hoaDon, PhieuGiamGia pgg) {
        BigDecimal tongTienMon = hoaDon.getTongTienMon() != null
                ? hoaDon.getTongTienMon()
                : BigDecimal.ZERO;

        return tinhTienGiam(pgg, tongTienMon);
    }

    private HoaDonResponse mapToHoaDonResponse(HoaDon hoaDon) {
        return HoaDonResponse.builder()
                .id(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .loaiHoaDon(hoaDon.getLoaiHoaDon())
                .thoiGianTao(hoaDon.getThoiGianTao())
                .thoiGianThanhToan(hoaDon.getThoiGianThanhToan())
                .tienCoc(hoaDon.getTienCoc())
                .tienThue(hoaDon.getTienThue())
                .tenNguoiNhan(hoaDon.getTenNguoiNhan())
                .soDienThoai(hoaDon.getSoDienThoai())
                .diaChiGiao(hoaDon.getDiaChiGiao())
                .phiVanChuyen(hoaDon.getPhiVanChuyen())
                .soTienDuocGiam(hoaDon.getSoTienDuocGiam())
                .tongTien(hoaDon.getTongTien())
                .trangThai(hoaDon.getTrangThai())
                .ghiChu(hoaDon.getGhiChu())
                .nguoiTao(hoaDon.getNguoiTao())
                .ngayCapNhat(hoaDon.getNgayCapNhat())
                .thoiGianMo(hoaDon.getThoiGianMo())
                .thoiGianDong(hoaDon.getThoiGianDong())
                .tongTienMon(hoaDon.getTongTienMon())
                .tienSauGiam(hoaDon.getTienSauGiam())
                .tienConLai(hoaDon.getTienConLai())
                .tongTienDaThanhToan(hoaDon.getTongTienDaThanhToan())
                .idPhieuGiamGia(hoaDon.getPhieuGiamGia() != null ? Long.valueOf(hoaDon.getPhieuGiamGia().getId()) : null)
                .idDatBan(hoaDon.getDatBan() != null ? Long.valueOf(hoaDon.getDatBan().getId()) : null)
                .idKhachHang(hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getId() : null)
                .tenKhachHang(hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getTenKhachHang() : null)
                .idNhanVien(hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getId() : null)
                .tenNhanVien(hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getTenNhanVien() : null)
                .listTenBan(layListTenBanTheoHoaDon(hoaDon.getId()))
                .tenTang(layTenTangTheoHoaDon(hoaDon.getId()))
                .build();
    }

    @Override
    public List<PhieuGiamGia> getDanhSachPhieuGiamGiaHopLe(Long idHoaDon) {
        capNhatTongTienHoaDon(idHoaDon);
        if (idHoaDon == null) {
            throw new RuntimeException("Thiếu id hóa đơn");
        }

        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        BigDecimal tongTienMon = hoaDon.getTongTienMon() != null
                ? hoaDon.getTongTienMon()
                : BigDecimal.ZERO;

        LocalDate today = LocalDate.now();

        return phieuGiamGiaRepository.findAll().stream()
                .filter(p -> isPhieuHopLe(p, tongTienMon, today))
                .filter(p -> Objects.equals(p.getKieuGiamGia(), 1)) // Lấy phiếu công khai
                .sorted((a, b) -> {
                    BigDecimal giamB = tinhTienGiamDuKienChoHoaDon(hoaDon, b);
                    BigDecimal giamA = tinhTienGiamDuKienChoHoaDon(hoaDon, a);
                    return giamB.compareTo(giamA);
                })
                .toList();
    }

    @Override
    @Transactional
    public HoaDonResponse apDungMaGiamGia(Long idHoaDon, String maGiamGia) {
        capNhatTongTienHoaDon(idHoaDon);
        log.info("[VOUCHER_AP_DUNG] idHoaDon={} | maGiamGia={}", idHoaDon, maGiamGia);

        if (idHoaDon == null) {
            throw new RuntimeException("Thiếu id hóa đơn");
        }

        if (maGiamGia == null || maGiamGia.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập mã giảm giá");
        }

        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        PhieuGiamGia phieu = phieuGiamGiaRepository
                .findByMaGiamGiaIgnoreCase(maGiamGia.trim())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá"));

        BigDecimal tongTienMon = hoaDon.getTongTienMon() != null
                ? hoaDon.getTongTienMon()
                : BigDecimal.ZERO;

        LocalDate today = LocalDate.now();

        // check hợp lệ
        if (!isPhieuHopLe(phieu, tongTienMon, today)) {
            throw new RuntimeException("Mã giảm giá không hợp lệ hoặc không đủ điều kiện áp dụng");
        }

        // ===== CHECK HỢP LỆ =====
        List<PhieuGiamGia> dsHopLeChung = getDanhSachPhieuGiamGiaHopLe(idHoaDon);
        boolean hopLeChung = dsHopLeChung.stream()
                .anyMatch(p -> Objects.equals(p.getId(), phieu.getId()));

        boolean hopLeTheoKhach = false;
        if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getId() != null) {
            List<PhieuGiamGia> dsTheoKhachHang = getDanhSachPhieuGiamGiaTheoKhachHang(idHoaDon);
            hopLeTheoKhach = dsTheoKhachHang.stream()
                    .anyMatch(p -> Objects.equals(p.getId(), phieu.getId()));
        }

        if (hoaDon.getKhachHang() == null || hoaDon.getKhachHang().getId() == null) {
            if (!hopLeChung) {
                throw new RuntimeException("Mã giảm giá không hợp lệ cho hóa đơn này");
            }
        } else {
            if (!hopLeChung && !hopLeTheoKhach) {
                throw new RuntimeException("Mã giảm giá không hợp lệ cho hóa đơn này");
            }
        }

        // ===== FIX LOGIC ĐỔI PHIẾU =====
        PhieuGiamGia phieuCu = hoaDon.getPhieuGiamGia();
        boolean laCungMotPhieu = phieuCu != null && Objects.equals(phieuCu.getId(), phieu.getId());

        // hoàn lại phiếu cũ nếu đổi
        if (phieuCu != null && !laCungMotPhieu) {
            phieuCu.setSoLuong((phieuCu.getSoLuong() == null ? 0 : phieuCu.getSoLuong()) + 1);
            phieuCu.setNgayCapNhat(DateTimeUtils.now());
            phieuGiamGiaRepository.save(phieuCu);
        }

        // trừ phiếu mới nếu không phải cùng phiếu
        if (!laCungMotPhieu) {
            if (phieu.getSoLuong() == null || phieu.getSoLuong() <= 0) {
                throw new RuntimeException("Phiếu giảm giá đã hết số lượng");
            }

            phieu.setSoLuong(phieu.getSoLuong() - 1);
            phieu.setNgayCapNhat(DateTimeUtils.now());
            phieuGiamGiaRepository.save(phieu);
        }

        // ===== TÍNH TIỀN =====
        BigDecimal soTienGiam = tinhTienGiam(phieu, tongTienMon);
        BigDecimal tienSauGiam = tongTienMon.subtract(soTienGiam);
        if (tienSauGiam.compareTo(BigDecimal.ZERO) < 0) {
            tienSauGiam = BigDecimal.ZERO;
        }

        BigDecimal tienCoc = hoaDon.getTienCoc() != null ? hoaDon.getTienCoc() : BigDecimal.ZERO;
        BigDecimal phiThuKhac = hoaDon.getPhiVanChuyen() != null ? hoaDon.getPhiVanChuyen() : BigDecimal.ZERO;

        BigDecimal tongTien = tongTienMon.add(phiThuKhac);
        
        BigDecimal tongDaThanhToan = hoaDon.getTongTienDaThanhToan() != null ? hoaDon.getTongTienDaThanhToan() : BigDecimal.ZERO;
        BigDecimal tienConLai = tongTien.subtract(soTienGiam).subtract(tienCoc).subtract(tongDaThanhToan);
        if (tienConLai.compareTo(BigDecimal.ZERO) < 0) {
            tienConLai = BigDecimal.ZERO;
        }

        // ===== SET HÓA ĐƠN =====
        hoaDon.setPhieuGiamGia(phieu);
        hoaDon.setSoTienDuocGiam(soTienGiam);
        hoaDon.setTienSauGiam(tienSauGiam);
        hoaDon.setTienConLai(tienConLai);
        hoaDon.setTongTien(tongTien);
        hoaDon.setNgayCapNhat(DateTimeUtils.now());

        hoaDonRepository.save(hoaDon);

        // ===== LOG =====
        lichSuHoaDonRepository.save(
                LichSuHoaDon.builder()
                        .hoaDon(hoaDon)
                        .thoiGian(DateTimeUtils.now())
                        .ngayTao(DateTimeUtils.now())
                        .ngayCapNhat(DateTimeUtils.now())
                        .hanhDong("AP_DUNG_MA_GIAM_GIA")
                        .trangThai(1)
                        .ghiChu("Áp dụng mã giảm giá: " + phieu.getMaGiamGia())
                        .nhanVien(hoaDon.getNhanVien())
                        .build()
        );

        return mapToHoaDonResponse(hoaDon);
    }

    @Override
    public List<PhieuGiamGia> getDanhSachPhieuGiamGiaTheoKhachHang(Long idHoaDon) {
        capNhatTongTienHoaDon(idHoaDon);
        if (idHoaDon == null) {
            throw new RuntimeException("Thiếu id hóa đơn");
        }

        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (hoaDon.getKhachHang() == null || hoaDon.getKhachHang().getId() == null) {
            return Collections.emptyList();
        }

        Long idKhachHang = hoaDon.getKhachHang().getId();
        BigDecimal tongTienMon = hoaDon.getTongTienMon() != null
                ? hoaDon.getTongTienMon()
                : BigDecimal.ZERO;
        LocalDate today = LocalDate.now();

        return khachHangGiamGiaRepository.findAllByKhachHangId(idKhachHang).stream()
                .map(KhachHangGiamGia::getPhieuGiamGia)
                .filter(Objects::nonNull)
                .filter(p -> isPhieuHopLe(p, tongTienMon, today))
                .filter(p -> Objects.equals(p.getKieuGiamGia(), 2)) // Lấy phiếu riêng tư
                .distinct()
                .sorted((a, b) -> {
                    BigDecimal giamB = tinhTienGiamDuKienChoHoaDon(hoaDon, b);
                    BigDecimal giamA = tinhTienGiamDuKienChoHoaDon(hoaDon, a);
                    return giamB.compareTo(giamA);
                })
                .toList();
    }

    @Override
    public PhieuGiamGia getPhieuGiamGiaTotNhat(Long idHoaDon) {
        capNhatTongTienHoaDon(idHoaDon);
        if (idHoaDon == null) {
            throw new RuntimeException("Thiếu id hóa đơn");
        }

        List<PhieuGiamGia> dsTheoKhachHang = getDanhSachPhieuGiamGiaTheoKhachHang(idHoaDon);
        if (!dsTheoKhachHang.isEmpty()) {
            return dsTheoKhachHang.get(0);
        }

        List<PhieuGiamGia> dsHopLeChung = getDanhSachPhieuGiamGiaHopLe(idHoaDon);
        if (dsHopLeChung.isEmpty()) {
            throw new RuntimeException("Không có phiếu giảm giá phù hợp");
        }

        return dsHopLeChung.get(0);
    }


    @Override
    @Transactional
    public CheckInResponse doiBan(DoiBanRequest request) {
        if (request.getIdHoaDon() == null) {
            throw new RuntimeException("Thiếu id hóa đơn");
        }

        if (request.getIdBanHienTai() == null) {
            throw new RuntimeException("Thiếu id bàn hiện tại");
        }

        List<Long> listIdBanMoi = request.getListIdBanMoi();

        if (listIdBanMoi == null || listIdBanMoi.isEmpty()) {
            if (request.getIdBanMoi() == null) {
                throw new RuntimeException("Thiếu id bàn mới");
            }
            listIdBanMoi = List.of(request.getIdBanMoi());
        }

        listIdBanMoi = listIdBanMoi.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (listIdBanMoi.isEmpty()) {
            throw new RuntimeException("Thiếu danh sách bàn mới");
        }

        if (listIdBanMoi.contains(request.getIdBanHienTai())) {
            throw new RuntimeException("Bàn mới phải khác bàn hiện tại");
        }

        HoaDon hoaDon = hoaDonRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        boolean tonTaiBanHienTai = hoaDonBanAnRepository.existsByHoaDon_IdAndBanAn_Id(
                request.getIdHoaDon(),
                request.getIdBanHienTai()
        );

        if (!tonTaiBanHienTai) {
            throw new RuntimeException("Bàn hiện tại không thuộc hóa đơn này");
        }

        List<BanAn> dsBanMoi = quanLyBanRepository.findAllById(
                listIdBanMoi.stream()
                        .map(Math::toIntExact)
                        .toList()
        );

        if (dsBanMoi.size() != listIdBanMoi.size()) {
            throw new RuntimeException("Có bàn mới không tồn tại");
        }

        boolean coBanKhongTrong = dsBanMoi.stream()
                .anyMatch(b -> !Objects.equals(b.getTrangThai(), TrangThaiBan.TRONG.getValue()));

        if (coBanKhongTrong) {
            throw new RuntimeException("Tất cả bàn mới phải ở trạng thái trống");
        }

        int tongSucChuaBanMoi = dsBanMoi.stream()
                .mapToInt(b -> b.getSucChuaToiDa() != null ? b.getSucChuaToiDa() : 0)
                .sum();

        int soNguoiDangDung = 0;

        if (hoaDon.getDatBan() != null && hoaDon.getDatBan().getSoNguoiToiDa() != null) {
            soNguoiDangDung = hoaDon.getDatBan().getSoNguoiToiDa();
        }

        if (soNguoiDangDung <= 0) {
            // Lấy tổng khách của toàn bộ hóa đơn thay vì chỉ bàn hiện tại (vì lát nữa sẽ xóa hết liên kết cũ)
            soNguoiDangDung = hoaDonBanAnRepository.findByHoaDon_Id(hoaDon.getId()).stream()
                    .mapToInt(lk -> lk.getSoKhach() != null ? lk.getSoKhach() : 0)
                    .sum();
        }

        if (soNguoiDangDung <= 0) {
            throw new RuntimeException("Không xác định được số người đang dùng");
        }

        // Bổ sung hỗ trợ lấy thêm "soKhachThem" khi người dùng Đổi bàn do thiếu chỗ
        Integer soKhachThem = null;
        try {
            try {
                java.lang.reflect.Method method = request.getClass().getMethod("getSoKhachThem");
                soKhachThem = (Integer) method.invoke(request);
            } catch (Exception e) {
                java.lang.reflect.Field field = request.getClass().getDeclaredField("soKhachThem");
                field.setAccessible(true);
                soKhachThem = (Integer) field.get(request);
            }
        } catch (Exception e) {
            throw new RuntimeException("HỆ THỐNG LỖI: Bạn chưa khai báo thuộc tính 'private Integer soKhachThem;' (và Getter/Setter) vào class DoiBanRequest.java!");
        }

        if (soKhachThem != null && soKhachThem > 0) {
            soNguoiDangDung += soKhachThem;
            if (hoaDon.getDatBan() != null) {
                DatBan db = hoaDon.getDatBan();
                db.setSoNguoiToiDa((db.getSoNguoiToiDa() != null ? db.getSoNguoiToiDa() : 0) + soKhachThem);
                datBanRepository.save(db);
            }
        }

        if (tongSucChuaBanMoi < soNguoiDangDung) {
            throw new RuntimeException(
                    "Bàn mới không đủ chỗ. Cần " + soNguoiDangDung
                            + " chỗ, hiện có " + tongSucChuaBanMoi + " chỗ"
            );
        }

        int soChoDu = tongSucChuaBanMoi - soNguoiDangDung;

        if (soChoDu > 3) {
            throw new RuntimeException(
                    "Không được đổi bàn dư quá 3 chỗ. Hiện đang dư " + soChoDu + " chỗ"
            );
        }

        List<Integer> idBanCuCuaHoaDon = layListIdBanTheoHoaDon(request.getIdHoaDon());

        hoaDonBanAnRepository.deleteByHoaDon_Id(request.getIdHoaDon());

        int tongKhachConLaiDoiBan = soNguoiDangDung;

        for (int i = 0; i < dsBanMoi.size(); i++) {
            BanAn banMoi = dsBanMoi.get(i);
            int sucChua = banMoi.getSucChuaToiDa() != null ? banMoi.getSucChuaToiDa() : 0;

            int khachNgoiBanNay = 0;
            if (tongKhachConLaiDoiBan > 0) {
                if (tongKhachConLaiDoiBan >= sucChua) {
                    khachNgoiBanNay = sucChua;
                    tongKhachConLaiDoiBan -= sucChua;
                } else {
                    khachNgoiBanNay = tongKhachConLaiDoiBan;
                    tongKhachConLaiDoiBan = 0;
                }
            }
            if (i == dsBanMoi.size() - 1 && tongKhachConLaiDoiBan > 0) {
                khachNgoiBanNay += tongKhachConLaiDoiBan;
                tongKhachConLaiDoiBan = 0;
            }

            HoaDonBanAn lienKetMoi = HoaDonBanAn.builder()
                    .hoaDon(hoaDon)
                    .banAn(banMoi)
                    .soKhach(khachNgoiBanNay)
                    .build();

            hoaDonBanAnRepository.save(lienKetMoi);
            quanLyBanRepository.updateTrangThai(Long.valueOf(banMoi.getId()), TrangThaiBan.DANG_SU_DUNG.getValue());
        }

        for (Integer idBanCu : idBanCuCuaHoaDon) {
            if (!listIdBanMoi.contains(Long.valueOf(idBanCu))) {
                BanAn banCu = quanLyBanRepository.findById(idBanCu).orElse(null);
                if (banCu != null) {
                    tuDongDonBan(banCu, hoaDon);
                }
            }
        }

        hoaDon.setNgayCapNhat(DateTimeUtils.now());
        hoaDonRepository.save(hoaDon);

        lichSuHoaDonRepository.save(
                LichSuHoaDon.builder()
                        .hoaDon(hoaDon)
                        .thoiGian(DateTimeUtils.now())
                        .ngayTao(DateTimeUtils.now())
                        .ngayCapNhat(DateTimeUtils.now())
                        .hanhDong("DOI_BAN")
                        .trangThai(1)
                        .ghiChu(
                                "Đổi bàn sang: " +
                                        dsBanMoi.stream()
                                                .map(BanAn::getTenBanAn)
                                                .collect(Collectors.joining(", ")) +
                                        (soKhachThem != null && soKhachThem > 0 ? " (Cộng thêm " + soKhachThem + " khách)" : "")
                        )
                        .nhanVien(hoaDon.getNhanVien())
                        .build()
        );

        Long idBanDaiDien = listIdBanMoi.get(0);

        return getChiTietCheckInSauThaoTac(
                request.getIdHoaDon(),
                idBanDaiDien,
                "Đổi bàn thành công"
        );
    }

    @Override
    @Transactional
    public CheckInResponse ghepBan(GhepBanRequest request) {
        if (request.getIdHoaDon() == null) {
            throw new RuntimeException("Thiếu id hóa đơn");
        }
        if (request.getIdBanHienTai() == null) {
            throw new RuntimeException("Thiếu id bàn hiện tại");
        }
        if (request.getListIdBanMuonGhep() == null || request.getListIdBanMuonGhep().isEmpty()) {
            throw new RuntimeException("Chưa chọn bàn để gộp");
        }

        HoaDon hoaDon = hoaDonRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        boolean tonTaiBanHienTai = hoaDonBanAnRepository.existsByHoaDon_IdAndBanAn_Id(
                request.getIdHoaDon(),
                request.getIdBanHienTai()
        );

        if (!tonTaiBanHienTai) {
            throw new RuntimeException("Bàn hiện tại không thuộc hóa đơn này");
        }

        int khachGhepVaoBanMoi = 0;
        Integer soKhachThem = null;
        try {
            try {
                java.lang.reflect.Method method = request.getClass().getMethod("getSoKhachThem");
                soKhachThem = (Integer) method.invoke(request);
            } catch (Exception e) {
                java.lang.reflect.Field field = request.getClass().getDeclaredField("soKhachThem");
                field.setAccessible(true);
                soKhachThem = (Integer) field.get(request);
            }
        } catch (Exception e) {
            throw new RuntimeException("HỆ THỐNG LỖI: Chưa khai báo thuộc tính 'private Integer soKhachThem;' có Getter/Setter trong file GhepBanRequest.java!");
        }

        if (soKhachThem == null || soKhachThem <= 0) {
            throw new RuntimeException("Số lượng khách thêm không hợp lệ (Bằng null hoặc <= 0)");
        }

        HoaDonBanAn lienKetHienTai = hoaDonBanAnRepository.findByHoaDon_IdAndBanAn_Id(
                request.getIdHoaDon(),
                request.getIdBanHienTai()
        ).orElse(null);

        if (lienKetHienTai != null) {
            int sucChua = lienKetHienTai.getBanAn().getSucChuaToiDa() != null ? lienKetHienTai.getBanAn().getSucChuaToiDa() : 0;
            int soKhach = lienKetHienTai.getSoKhach() != null ? lienKetHienTai.getSoKhach() :
                    (hoaDon.getDatBan() != null && hoaDon.getDatBan().getSoNguoiToiDa() != null ? hoaDon.getDatBan().getSoNguoiToiDa() : 0);
            int choTrong = Math.max(sucChua - soKhach, 0);

            if (request.getListIdBanMuonGhep().size() == 1 && String.valueOf(request.getListIdBanMuonGhep().get(0)).equals(String.valueOf(request.getIdBanHienTai()))) {
                if (choTrong >= soKhachThem) {
                    lienKetHienTai.setSoKhach(soKhach + soKhachThem);
                    hoaDonBanAnRepository.save(lienKetHienTai);
                    lichSuHoaDonRepository.save(
                            LichSuHoaDon.builder()
                                    .hoaDon(hoaDon)
                                    .thoiGian(DateTimeUtils.now())
                                    .ngayTao(DateTimeUtils.now())
                                    .ngayCapNhat(DateTimeUtils.now())
                                    .hanhDong("THEM_KHACH")
                                    .trangThai(1)
                                    .ghiChu("Thêm " + soKhachThem + " khách vào bàn hiện tại")
                                    .nhanVien(hoaDon.getNhanVien())
                                    .build()
                    );
                    return getChiTietCheckInSauThaoTac(request.getIdHoaDon(), request.getIdBanHienTai(), "Thêm khách vào bàn đang dùng thành công");
                } else {
                    throw new RuntimeException("Bàn hiện tại chỉ còn " + choTrong + " chỗ trống, không đủ cho " + soKhachThem + " khách! Vui lòng thao tác Đổi bàn hoặc chọn thêm bàn trống để ghép.");
                }
            }

            if (choTrong >= soKhachThem) {
                throw new RuntimeException("Yêu cầu ghép vào bàn đang dùng vì bàn hiện tại vẫn đủ chỗ!");
            }

            if (choTrong > 0) {
                lienKetHienTai.setSoKhach(sucChua);
                hoaDonBanAnRepository.save(lienKetHienTai);
                khachGhepVaoBanMoi = soKhachThem - choTrong;
            } else {
                khachGhepVaoBanMoi = soKhachThem;
            }
        } else {
            khachGhepVaoBanMoi = soKhachThem;
        }

        List<Long> dsBanMuonGhep = new ArrayList<>();
        for (Long id : request.getListIdBanMuonGhep()) {
            if (id != null && !String.valueOf(id).equals(String.valueOf(request.getIdBanHienTai()))) {
                dsBanMuonGhep.add(id);
            }
        }

        int tongKhachConLai = khachGhepVaoBanMoi;

        for (int i = 0; i < dsBanMuonGhep.size(); i++) {
            Long idBanMuonGhep = dsBanMuonGhep.get(i);
            boolean daTonTaiTrongHoaDon = hoaDonBanAnRepository.existsByHoaDon_IdAndBanAn_Id(request.getIdHoaDon(), idBanMuonGhep);

            if (daTonTaiTrongHoaDon) {
                continue;
            }

            BanAn banMuonGhep = quanLyBanRepository.findById(Math.toIntExact(idBanMuonGhep))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn muốn gộp: " + idBanMuonGhep));

            if (!Integer.valueOf(1).equals(banMuonGhep.getTrangThai())) {
                throw new RuntimeException("Bàn " + banMuonGhep.getTenBanAn() + " không ở trạng thái trống");
            }

            int sucChuaBanMoi = banMuonGhep.getSucChuaToiDa() != null ? banMuonGhep.getSucChuaToiDa() : 0;
            int khachNgoiBanNay = 0;
            if (tongKhachConLai > 0) {
                if (tongKhachConLai >= sucChuaBanMoi) {
                    khachNgoiBanNay = sucChuaBanMoi;
                    tongKhachConLai -= sucChuaBanMoi;
                } else {
                    khachNgoiBanNay = tongKhachConLai;
                    tongKhachConLai = 0;
                }
            }
            if (i == dsBanMuonGhep.size() - 1 && tongKhachConLai > 0) {
                khachNgoiBanNay += tongKhachConLai;
                tongKhachConLai = 0;
            }

            HoaDonBanAn lienKetMoi = HoaDonBanAn.builder()
                    .hoaDon(hoaDon)
                    .banAn(banMuonGhep)
                    .soKhach(khachNgoiBanNay)
                    .build();

            hoaDonBanAnRepository.save(lienKetMoi);

            quanLyBanRepository.updateTrangThai(idBanMuonGhep, 3);
        }

        hoaDon.setNgayCapNhat(DateTimeUtils.now());
        hoaDonRepository.save(hoaDon);

        String tenBanGhep = request.getListIdBanMuonGhep().stream()
                .filter(Objects::nonNull)
                .map(id -> quanLyBanRepository.findById(Math.toIntExact(id))
                        .map(BanAn::getTenBanAn)
                        .orElse("Bàn " + id))
                .collect(Collectors.joining(", "));

        lichSuHoaDonRepository.save(
                LichSuHoaDon.builder()
                        .hoaDon(hoaDon)
                        .thoiGian(DateTimeUtils.now())
                        .ngayTao(DateTimeUtils.now())
                        .ngayCapNhat(DateTimeUtils.now())
                        .hanhDong("GHEP_CHO_NGOI")
                        .trangThai(1)
                        .ghiChu("Ghép thêm bàn " + tenBanGhep + " vào hóa đơn " + hoaDon.getMaHoaDon())
                        .nhanVien(hoaDon.getNhanVien())
                        .build()
        );

        return getChiTietCheckInSauThaoTac(
                request.getIdHoaDon(),
                request.getIdBanHienTai(),
                "Gộp bàn thành công"
        );
    }

    @Override
    @Transactional
    public CheckInResponse tachBan(TachBanRequest request) {
        if (request.getIdHoaDon() == null) throw new RuntimeException("Thiếu id hóa đơn cần tách");
        if (request.getIdBanGoc() == null) throw new RuntimeException("Thiếu id bàn gốc");
        if (request.getDanhSachBanMoi() == null || request.getDanhSachBanMoi().isEmpty()) {
            throw new RuntimeException("Chưa chọn bàn mới để tách");
        }

        HoaDon hoaDon = hoaDonRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn cần tách"));

        // Lấy bản ghi Liên kết Bàn - Hóa đơn của bàn gốc
        HoaDonBanAn lienKetGoc = hoaDonBanAnRepository.findByHoaDon_IdAndBanAn_Id(request.getIdHoaDon(), request.getIdBanGoc())
                .orElseThrow(() -> new RuntimeException("Hóa đơn này không thuộc bàn gốc"));

        BanAn banGoc = lienKetGoc.getBanAn();

        // Xác định số khách hiện tại của bàn gốc
        int soKhachHienTai = lienKetGoc.getSoKhach() != null ? lienKetGoc.getSoKhach() :
                (hoaDon.getDatBan() != null && hoaDon.getDatBan().getSoNguoiToiDa() != null ? hoaDon.getDatBan().getSoNguoiToiDa() : 0);

        if (soKhachHienTai <= 0) {
            throw new RuntimeException("Không xác định được số khách hiện tại ở bàn gốc để tách");
        }

        int tongKhachChuyen = 0;
        List<String> tenCacBanMoi = new ArrayList<>();

        // ===== 1. XỬ LÝ CHUYỂN KHÁCH SANG CÁC BÀN MỚI =====
        for (TachBanRequest.ChiTietTachBan chiTiet : request.getDanhSachBanMoi()) {
            if (chiTiet.getIdBan().equals(request.getIdBanGoc())) {
                throw new RuntimeException("Bàn mới không được trùng với bàn gốc");
            }

            BanAn banMoi = quanLyBanRepository.findById(Math.toIntExact(chiTiet.getIdBan()))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn mới: " + chiTiet.getIdBan()));

            if (!Objects.equals(banMoi.getTrangThai(), TrangThaiBan.TRONG.getValue()) &&
                !Objects.equals(banMoi.getTrangThai(), TrangThaiBan.DANG_SU_DUNG.getValue())) {
                throw new RuntimeException("Bàn " + banMoi.getTenBanAn() + " không ở trạng thái trống hoặc đang sử dụng");
            }

            boolean daLienKet = hoaDonBanAnRepository.existsByHoaDon_IdAndBanAn_Id(Math.toIntExact(hoaDon.getId()), banMoi.getId());
            if (daLienKet) {
                throw new RuntimeException("Bàn " + banMoi.getTenBanAn() + " đã nằm trong cùng hóa đơn, không thể tách khách vào");
            }

            int soKhachHienTaiBanMoi = 0;
            if (Objects.equals(banMoi.getTrangThai(), TrangThaiBan.DANG_SU_DUNG.getValue())) {
                List<Integer> tts = List.of(
                        TrangThaiHoaDon.VUA_TAO.getCode(),
                        TrangThaiHoaDon.CHO_COC.getCode(),
                        TrangThaiHoaDon.DA_COC.getCode(),
                        TrangThaiHoaDon.DA_XAC_NHAN.getCode(),
                        TrangThaiHoaDon.KHACH_DEN.getCode(),
                        TrangThaiHoaDon.DANG_PHUC_VU.getCode(),
                        TrangThaiHoaDon.CHO_THANH_TOAN.getCode()
                );
                List<HoaDonBanAn> hdbaList = hoaDonBanAnRepository.findHoaDonDangHoatDongByBan(banMoi.getId(), tts);
                soKhachHienTaiBanMoi = hdbaList.stream().mapToInt(lk -> lk.getSoKhach() != null ? lk.getSoKhach() : 0).sum();
            }

            int soKhachQuaBanMoi = chiTiet.getSoKhach() != null ? chiTiet.getSoKhach() : 0;
            if (soKhachQuaBanMoi <= 0) {
                throw new RuntimeException("Số khách chuyển sang bàn " + banMoi.getTenBanAn() + " phải lớn hơn 0");
            }

            int sucChuaBanMoi = banMoi.getSucChuaToiDa() != null ? banMoi.getSucChuaToiDa() : 0;
            if (soKhachHienTaiBanMoi + soKhachQuaBanMoi > sucChuaBanMoi) {
                throw new RuntimeException("Bàn " + banMoi.getTenBanAn() + " chỉ còn " + Math.max(0, sucChuaBanMoi - soKhachHienTaiBanMoi) + " chỗ trống, không đủ cho " + soKhachQuaBanMoi + " người");
            }

            tongKhachChuyen += soKhachQuaBanMoi;

            // Tạo liên kết Hóa Đơn - Bàn Ăn Mới
            hoaDonBanAnRepository.save(
                    HoaDonBanAn.builder()
                            .hoaDon(hoaDon)
                            .banAn(banMoi)
                            .soKhach(soKhachQuaBanMoi) // Set số khách sang bàn mới
                            .build()
            );

            // Cập nhật trạng thái bàn mới
            banMoi.setTrangThai(TrangThaiBan.DANG_SU_DUNG.getValue());
            banMoi.setNgayCapNhat(DateTimeUtils.now());
            quanLyBanRepository.save(banMoi);

            tenCacBanMoi.add(banMoi.getTenBanAn());

            if (hoaDon.getDatBan() != null && hoaDon.getDatBan().getBanAns() != null) {
                DatBan db = hoaDon.getDatBan();
                boolean exists = db.getBanAns().stream().anyMatch(b -> b.getId().equals(banMoi.getId()));
                if (!exists) {
                    db.getBanAns().add(banMoi);
                    datBanRepository.save(db);
                }
            }
        }

        // ===== 2. XỬ LÝ BÀN GỐC =====
        if (tongKhachChuyen > soKhachHienTai) {
            throw new RuntimeException("Tổng số khách tách đi (" + tongKhachChuyen + ") vượt quá số khách hiện tại ở bàn gốc (" + soKhachHienTai + ")");
        }

        int soKhachConLai = soKhachHienTai - tongKhachChuyen;

        if (soKhachConLai == 0) {
            // Khách chuyển đi hết toàn bộ -> Xóa liên kết và trả lại bàn trống
            hoaDonBanAnRepository.delete(lienKetGoc);

            // Check thêm xem bàn gốc có bị hóa đơn nào khác ghép chung không, nếu không mới set TRỐNG
            List<Integer> trangThaiDangHoatDong = List.of(
                    TrangThaiHoaDon.VUA_TAO.getCode(),
                    TrangThaiHoaDon.CHO_COC.getCode(),
                    TrangThaiHoaDon.DA_COC.getCode(),
                    TrangThaiHoaDon.DA_XAC_NHAN.getCode(),
                    TrangThaiHoaDon.KHACH_DEN.getCode(),
                    TrangThaiHoaDon.DANG_PHUC_VU.getCode(),
                    TrangThaiHoaDon.CHO_THANH_TOAN.getCode()
            );
            List<HoaDonBanAn> hdConLaiOBan = hoaDonBanAnRepository.findHoaDonDangHoatDongByBan(
                    Math.toIntExact(request.getIdBanGoc()),
                    trangThaiDangHoatDong
            );
            if (hdConLaiOBan.isEmpty() || (hdConLaiOBan.size() == 1 && hdConLaiOBan.get(0).getId().equals(lienKetGoc.getId()))) {
                tuDongDonBan(banGoc, hoaDon);
            } else {
                banGoc.setNgayCapNhat(DateTimeUtils.now());
                quanLyBanRepository.save(banGoc);
            }

            if (hoaDon.getDatBan() != null && hoaDon.getDatBan().getBanAns() != null) {
                DatBan db = hoaDon.getDatBan();
                db.getBanAns().removeIf(b -> b.getId().equals(banGoc.getId()));
                datBanRepository.save(db);
            }
        } else {
            // Vẫn còn khách ngồi lại bàn gốc -> Cập nhật lại số lượng khách
            lienKetGoc.setSoKhach(soKhachConLai);
            hoaDonBanAnRepository.save(lienKetGoc);
            banGoc.setNgayCapNhat(DateTimeUtils.now());
            quanLyBanRepository.save(banGoc);
        }

        // ===== 3. CẬP NHẬT LỊCH SỬ =====
        hoaDon.setNgayCapNhat(DateTimeUtils.now());
        hoaDonRepository.save(hoaDon);

        lichSuHoaDonRepository.save(
                LichSuHoaDon.builder()
                        .hoaDon(hoaDon)
                        .thoiGian(DateTimeUtils.now())
                        .ngayTao(DateTimeUtils.now())
                        .ngayCapNhat(DateTimeUtils.now())
                        .hanhDong("TACH_BAN")
                        .trangThai(1)
                        .ghiChu("Đã tách " + tongKhachChuyen + " khách từ bàn " + banGoc.getTenBanAn() + " sang các bàn: " + String.join(", ", tenCacBanMoi))
                        .nhanVien(hoaDon.getNhanVien())
                        .build()
        );

        return getChiTietCheckInSauThaoTac(
                request.getIdHoaDon(),
                request.getDanhSachBanMoi().get(0).getIdBan(),
                "Tách khách sang bàn mới thành công"
        );
    }

    @Override
    @Transactional
    public CheckInResponse ghepHaiBanDangDung(GhepBanDangDungRequest request) {
        if (request.getIdHoaDonChinh() == null) {
            throw new RuntimeException("Thiếu id hóa đơn chính");
        }
        if (request.getIdHoaDonGhep() == null) {
            throw new RuntimeException("Thiếu id hóa đơn cần chuyển bàn");
        }
        if (request.getIdBanChinh() == null) {
            throw new RuntimeException("Thiếu id bàn đích");
        }
        if (request.getIdBanGhep() == null) {
            throw new RuntimeException("Thiếu id bàn nguồn");
        }
        if (Objects.equals(request.getIdHoaDonChinh(), request.getIdHoaDonGhep())) {
            throw new RuntimeException("Không thể ghép cùng một hóa đơn");
        }

        HoaDon hoaDonChinh = hoaDonRepository.findById(request.getIdHoaDonChinh())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn chính"));

        HoaDon hoaDonGhep = hoaDonRepository.findById(request.getIdHoaDonGhep())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn cần chuyển bàn"));

        BanAn banChinh = quanLyBanRepository.findById(Math.toIntExact(request.getIdBanChinh()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn đích"));

        BanAn banGhep = quanLyBanRepository.findById(Math.toIntExact(request.getIdBanGhep()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn nguồn"));

        List<Integer> trangThaiDangHoatDong = List.of(
                TrangThaiHoaDon.VUA_TAO.getCode(),
                TrangThaiHoaDon.CHO_COC.getCode(),
                TrangThaiHoaDon.DA_COC.getCode(),
                TrangThaiHoaDon.DA_XAC_NHAN.getCode(),
                TrangThaiHoaDon.KHACH_DEN.getCode(),
                TrangThaiHoaDon.DANG_PHUC_VU.getCode(),
                TrangThaiHoaDon.CHO_THANH_TOAN.getCode()
        );

        if (!trangThaiDangHoatDong.contains(hoaDonChinh.getTrangThai())) {
            throw new RuntimeException("Hóa đơn chính không còn hoạt động");
        }

        if (!trangThaiDangHoatDong.contains(hoaDonGhep.getTrangThai())) {
            throw new RuntimeException("Hóa đơn cần chuyển bàn không còn hoạt động");
        }

        boolean banChinhThuocHoaDonChinh =
                hoaDonBanAnRepository.existsByHoaDon_IdAndBanAn_Id(
                        request.getIdHoaDonChinh(),
                        request.getIdBanChinh()
                );

        if (!banChinhThuocHoaDonChinh) {
            throw new RuntimeException("Bàn đích không thuộc hóa đơn chính");
        }

        boolean banGhepThuocHoaDonGhep =
                hoaDonBanAnRepository.existsByHoaDon_IdAndBanAn_Id(
                        request.getIdHoaDonGhep(),
                        request.getIdBanGhep()
                );

        if (!banGhepThuocHoaDonGhep) {
            throw new RuntimeException("Bàn nguồn không thuộc hóa đơn cần chuyển");
        }

        HoaDonBanAn lienKetChinh = hoaDonBanAnRepository.findByHoaDon_IdAndBanAn_Id(request.getIdHoaDonChinh(), request.getIdBanChinh()).orElse(null);
        Integer soKhachBanChinh = lienKetChinh != null && lienKetChinh.getSoKhach() != null ? lienKetChinh.getSoKhach() :
                (hoaDonChinh.getDatBan() != null
                && hoaDonChinh.getDatBan().getSoNguoiToiDa() != null
                ? hoaDonChinh.getDatBan().getSoNguoiToiDa()
                : 0);

        HoaDonBanAn lienKetGhep = hoaDonBanAnRepository.findByHoaDon_IdAndBanAn_Id(request.getIdHoaDonGhep(), request.getIdBanGhep()).orElse(null);
        Integer soKhachBanGhep = lienKetGhep != null && lienKetGhep.getSoKhach() != null ? lienKetGhep.getSoKhach() :
                (hoaDonGhep.getDatBan() != null
                && hoaDonGhep.getDatBan().getSoNguoiToiDa() != null
                ? hoaDonGhep.getDatBan().getSoNguoiToiDa()
                : 0);

        Integer tongKhachSauGhep = soKhachBanChinh + soKhachBanGhep;

        Integer sucChuaBanChinh = banChinh.getSucChuaToiDa() != null
                ? banChinh.getSucChuaToiDa()
                : 0;

        if (tongKhachSauGhep > sucChuaBanChinh) {
            throw new RuntimeException(
                    "Không thể ghép bàn. Bàn "
                            + banChinh.getTenBanAn()
                            + " chỉ có sức chứa "
                            + sucChuaBanChinh
                            + " người, hiện đang có "
                            + soKhachBanChinh
                            + " người, không thể ghép thêm "
                            + soKhachBanGhep
                            + " người"
            );
        }

        boolean hoaDonGhepDaNamOBanChinh =
                hoaDonBanAnRepository.existsByHoaDon_IdAndBanAn_Id(
                        request.getIdHoaDonGhep(),
                        request.getIdBanChinh()
                );


        HoaDonBanAn lienKetCu = hoaDonBanAnRepository.findByHoaDon_IdAndBanAn_Id(request.getIdHoaDonGhep(), request.getIdBanGhep()).orElse(null);
        int soKhachChuyen = lienKetCu != null && lienKetCu.getSoKhach() != null ? lienKetCu.getSoKhach() : soKhachBanGhep;

        if (!hoaDonGhepDaNamOBanChinh) {
            hoaDonBanAnRepository.save(
                    HoaDonBanAn.builder()
                            .hoaDon(hoaDonGhep)
                            .banAn(banChinh)
                            .soKhach(soKhachChuyen)
                            .build()
            );

            if (hoaDonGhep.getDatBan() != null && hoaDonGhep.getDatBan().getBanAns() != null) {
                DatBan dbGhep = hoaDonGhep.getDatBan();
                boolean exists = dbGhep.getBanAns().stream().anyMatch(b -> b.getId().equals(banChinh.getId()));
                if (!exists) {
                    dbGhep.getBanAns().add(banChinh);
                    datBanRepository.save(dbGhep);
                }
            }
        }

        hoaDonBanAnRepository.deleteByHoaDon_IdAndBanAn_Id(
                request.getIdHoaDonGhep(),
                request.getIdBanGhep()
        );

        if (hoaDonGhep.getDatBan() != null && hoaDonGhep.getDatBan().getBanAns() != null) {
            DatBan dbGhep = hoaDonGhep.getDatBan();
            dbGhep.getBanAns().removeIf(b -> b.getId().equals(banGhep.getId()));
            datBanRepository.save(dbGhep);
        }

        banChinh.setTrangThai(TrangThaiBan.DANG_SU_DUNG.getValue());
        banChinh.setNgayCapNhat(DateTimeUtils.now());
        quanLyBanRepository.save(banChinh);

        List<HoaDonBanAn> hoaDonConLaiOBanNguon =
                hoaDonBanAnRepository.findHoaDonDangHoatDongByBan(
                        Math.toIntExact(request.getIdBanGhep()),
                        trangThaiDangHoatDong
                );

        if (hoaDonConLaiOBanNguon.isEmpty()) {
            tuDongDonBan(banGhep, hoaDonGhep);
        }

        hoaDonChinh.setNgayCapNhat(DateTimeUtils.now());
        hoaDonRepository.save(hoaDonChinh);

        hoaDonGhep.setNgayCapNhat(DateTimeUtils.now());
        hoaDonRepository.save(hoaDonGhep);

        lichSuHoaDonRepository.save(
                LichSuHoaDon.builder()
                        .hoaDon(hoaDonChinh)
                        .thoiGian(DateTimeUtils.now())
                        .ngayTao(DateTimeUtils.now())
                        .ngayCapNhat(DateTimeUtils.now())
                        .hanhDong("GHEP_CHO_NGOI")
                        .trangThai(1)
                        .ghiChu("Bàn " + banGhep.getTenBanAn()
                                + " được ghép chỗ ngồi vào bàn "
                                + banChinh.getTenBanAn()
                                + ". Hóa đơn vẫn tách riêng.")
                        .nhanVien(hoaDonChinh.getNhanVien())
                        .build()
        );

        lichSuHoaDonRepository.save(
                LichSuHoaDon.builder()
                        .hoaDon(hoaDonGhep)
                        .thoiGian(DateTimeUtils.now())
                        .ngayTao(DateTimeUtils.now())
                        .ngayCapNhat(DateTimeUtils.now())
                        .hanhDong("CHUYEN_CHO_NGOI")
                        .trangThai(1)
                        .ghiChu("Hóa đơn " + hoaDonGhep.getMaHoaDon()
                                + " chuyển từ bàn "
                                + banGhep.getTenBanAn()
                                + " sang dùng chung bàn "
                                + banChinh.getTenBanAn()
                                + ". Không gộp hóa đơn.")
                        .nhanVien(hoaDonGhep.getNhanVien())
                        .build()
        );

        return getChiTietCheckInSauThaoTac(
                request.getIdHoaDonChinh(),
                request.getIdBanChinh(),
                "Ghép chỗ ngồi thành công, hóa đơn vẫn tách riêng"
        );
    }

    @Override
    public List<HoaDonDangDungTheoBanResponse> getHoaDonDangDungTheoBan(Integer idBanAn) {
        BanAn banAn = quanLyBanRepository.findById(idBanAn)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn ăn"));

        List<Integer> trangThaiDangHoatDong = List.of(
                TrangThaiHoaDon.VUA_TAO.getCode(),
                TrangThaiHoaDon.CHO_COC.getCode(),
                TrangThaiHoaDon.DA_COC.getCode(),
                TrangThaiHoaDon.DA_XAC_NHAN.getCode(),
                TrangThaiHoaDon.KHACH_DEN.getCode(),
                TrangThaiHoaDon.DANG_PHUC_VU.getCode(),
                TrangThaiHoaDon.CHO_THANH_TOAN.getCode()
        );

        List<HoaDonBanAn> dsLienKetGoc =
                hoaDonBanAnRepository.findHoaDonDangHoatDongByBan(
                        idBanAn,
                        trangThaiDangHoatDong
                );

        // --- BỘ LỌC THOÁNG: Ẩn các hóa đơn đặt trước còn xa (>1h) ---
        LocalDateTime bayGio = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        List<HoaDonBanAn> dsLienKetDaLoc = dsLienKetGoc.stream().filter(lk -> {
            HoaDon hd = lk.getHoaDon();
            if (hd.getDatBan() != null && hd.getDatBan().getThoiGianDat() != null) {
                return !hd.getDatBan().getThoiGianDat().isAfter(bayGio.plusHours(3));
            }
            return true; // Khách đang ngồi thật thì giữ lại
        }).toList();
        // -----------------------------------------------------------

        return dsLienKetDaLoc.stream()
                .map(item -> {
                    HoaDon hd = item.getHoaDon();
                    DatBan db = hd.getDatBan();

                    List<String> listTenBan = layListTenBanTheoHoaDon(hd.getId());
                    
                    int tongKhachHoaDon = hoaDonBanAnRepository.findByHoaDon_Id(hd.getId()).stream()
                            .mapToInt(lk -> lk.getSoKhach() != null ? lk.getSoKhach() : 0)
                            .sum();
                    if (tongKhachHoaDon == 0 && db != null && db.getSoNguoiToiDa() != null) {
                        tongKhachHoaDon = db.getSoNguoiToiDa();
                    }

                    return HoaDonDangDungTheoBanResponse.builder()
                            .idHoaDon(hd.getId())
                            .maHoaDon(hd.getMaHoaDon())
                            .trangThaiHoaDon(hd.getTrangThai())

                            .idBanAn(banAn.getId())
                            .maBanAn(banAn.getMaBanAn())
                            .tenBanAn(banAn.getTenBanAn())
                            .khuVucId(banAn.getKhuVuc() != null ? banAn.getKhuVuc().getId() : null)
                            .tenKhuVuc(banAn.getKhuVuc() != null ? banAn.getKhuVuc().getTenKhuVuc() : null)

                            .idDatBan(db != null ? db.getId() : null)
                            .maDatBan(db != null ? db.getMaDatBan() : null)

                            .tenKhachHang(
                                    db != null && db.getKhachHang() != null
                                            ? db.getKhachHang().getTenKhachHang()
                                            : "Khách vãng lai"
                            )

                            .soNguoi(
                                    item.getSoKhach() != null ? item.getSoKhach() : 0
                            )
                            .soNguoiDat(tongKhachHoaDon)

                            .soDienThoai(
                                    db != null && db.getKhachHang() != null
                                            ? db.getKhachHang().getSoDienThoai()
                                            : null
                            )

                            .tongTienMon(hd.getTongTienMon())
                            .tongTien(hd.getTongTien())
                            .tienConLai(hd.getTienConLai())
                            .tongTienDaThanhToan(hd.getTongTienDaThanhToan())

                            .thoiGianTao(hd.getThoiGianTao())
                            .thoiGianMo(hd.getThoiGianMo())
                            .listTenBan(listTenBan)
                            .build();
                })
                .toList();
    }

    @Override
    public CheckInDetailResponse getChiTietCheckInTheoHoaDon(Integer idBanAn, Long idHoaDon) {
        BanAn banAn = quanLyBanRepository.findById(idBanAn)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn ăn"));

        HoaDon hoaDon = hoaDonRepository.findDetailById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // LẤY TRỰC TIẾP BẢN GHI LIÊN KẾT ĐỂ LẤY SỐ KHÁCH
        HoaDonBanAn lienKetHoaDonBanAn = hoaDonBanAnRepository.findByHoaDon_IdAndBanAn_Id(
                idHoaDon,
                Long.valueOf(idBanAn)
        ).orElse(null);

        if (lienKetHoaDonBanAn == null) {
            throw new RuntimeException("Hóa đơn này không thuộc bàn đang chọn");
        }

        DatBan datBan = hoaDon.getDatBan();

        List<Integer> listIdBan = layListIdBanTheoHoaDon(hoaDon.getId());
        List<String> listTenBan = layListTenBanTheoHoaDon(hoaDon.getId());

        List<Integer> trangThaiDangHoatDong = List.of(
                TrangThaiHoaDon.VUA_TAO.getCode(),
                TrangThaiHoaDon.CHO_COC.getCode(),
                TrangThaiHoaDon.DA_COC.getCode(),
                TrangThaiHoaDon.DA_XAC_NHAN.getCode(),
                TrangThaiHoaDon.KHACH_DEN.getCode(),
                TrangThaiHoaDon.DANG_PHUC_VU.getCode(),
                TrangThaiHoaDon.CHO_THANH_TOAN.getCode()
        );

        List<HoaDonBanAn> dsHoaDonDangHoatDong =
                hoaDonBanAnRepository.findHoaDonDangHoatDongByBan(idBanAn, trangThaiDangHoatDong);

        int soHoaDonDangHoatDong = dsHoaDonDangHoatDong.size();
        boolean coNhieuHoaDon = soHoaDonDangHoatDong >= 2;

        // XÁC ĐỊNH SỐ KHÁCH THỰC TẾ ĐANG NGỒI Ở BÀN NÀY
        Integer soKhachThucTe = lienKetHoaDonBanAn.getSoKhach();
        if (soKhachThucTe == null || soKhachThucTe == 0) {
            soKhachThucTe = datBan != null ? datBan.getSoNguoiToiDa() : null;
        }
        
        // TỔNG KHÁCH CỦA TOÀN BỘ HÓA ĐƠN NÀY
        Integer tongKhachHoaDon = hoaDonBanAnRepository.findByHoaDon_Id(idHoaDon).stream()
                .mapToInt(lk -> lk.getSoKhach() != null ? lk.getSoKhach() : 0)
                .sum();

        Integer trangThaiBanThucTe = banAn.getTrangThai();
        if (soHoaDonDangHoatDong > 0) {
            trangThaiBanThucTe = TrangThaiBan.DANG_SU_DUNG.getValue();
        }

        return CheckInDetailResponse.builder()
                .idBan(banAn.getId())
                .maBan(banAn.getMaBanAn())
                .tenBan(banAn.getTenBanAn())
                .sucChuaToiDa(banAn.getSucChuaToiDa())
                .trangThaiBan(trangThaiBanThucTe)
                .idKhuVuc(banAn.getKhuVuc() != null ? banAn.getKhuVuc().getId() : null)
                .tenKhuVuc(banAn.getKhuVuc() != null ? banAn.getKhuVuc().getTenKhuVuc() : null)

                .idDatBan(datBan != null ? datBan.getId() : null)
                .maDatBan(datBan != null ? datBan.getMaDatBan() : null)
                .thoiGianDat(datBan != null ? datBan.getThoiGianDat() : null)

                .soNguoiDat(tongKhachHoaDon != null && tongKhachHoaDon > 0 ? tongKhachHoaDon : (datBan != null ? datBan.getSoNguoiToiDa() : 0))
                .soNguoiDangDung(soKhachThucTe)

                .ghiChuDatBan(datBan != null ? datBan.getGhiChu() : null)
                .tienCoc(hoaDon != null && hoaDon.getTienCoc() != null 
                        ? hoaDon.getTienCoc() 
                        : (datBan != null ? datBan.getTienCoc() : null))
                .trangThaiCoc(datBan != null ? datBan.getTrangThaiCoc() : null)

                .trangThaiDatBan(datBan != null ? datBan.getTrangThai() : null)
                .hinhThucDat(datBan != null ? datBan.getHinhThucDat() : null)
                .ngayTaoDatBan(datBan != null ? datBan.getNgayTao() : null)
                .ngayCapNhatDatBan(datBan != null ? datBan.getNgayCapNhat() : null)
                .hanGiuBan(datBan != null ? tinhHanGiuBan(datBan) : null)
                .hanThanhToanCoc(datBan != null ? datBan.getHanThanhToanCoc() : null)

                .idKhachHang(datBan != null && datBan.getKhachHang() != null ? datBan.getKhachHang().getId() : null)
                .tenKhachHang(datBan != null && datBan.getKhachHang() != null ? datBan.getKhachHang().getTenKhachHang() : "Khách vãng lai")
                .soDienThoai(datBan != null && datBan.getKhachHang() != null ? datBan.getKhachHang().getSoDienThoai() : null)

                .idHoaDon(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .trangThaiHoaDon(hoaDon.getTrangThai())

                .thoiGianCheckIn(hoaDon.getThoiGianMo())
                .thoiGianTaoHoaDon(hoaDon.getThoiGianTao())
                .thoiGianMo(hoaDon.getThoiGianMo())
                .thoiGianThanhToan(hoaDon.getThoiGianThanhToan())
                .thoiGianDong(hoaDon.getThoiGianDong())

                .idNhanVien(hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getId() : null)
                .tenNhanVien(hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getTenNhanVien() : null)
                .nguoiTaoHoaDon(hoaDon.getNguoiTao() != null ? String.valueOf(hoaDon.getNguoiTao()) : null)

                .tongTienMon(hoaDon.getTongTienMon())
                .tongTien(hoaDon.getTongTien())
                .soTienDuocGiam(hoaDon.getSoTienDuocGiam())
                .tienSauGiam(hoaDon.getTienSauGiam())
                .vatPercent(hoaDon.getVatPercent())
                .tienConLai(hoaDon.getTienConLai())
                .tongTienDaThanhToan(hoaDon.getTongTienDaThanhToan())

                .idPhieuGiamGia(
                        hoaDon.getPhieuGiamGia() != null
                                ? Long.valueOf(hoaDon.getPhieuGiamGia().getId())
                                : null
                )
                .maGiamGia(
                        hoaDon.getPhieuGiamGia() != null
                                ? hoaDon.getPhieuGiamGia().getMaGiamGia()
                                : null
                )
                .tenGiamGia(
                        hoaDon.getPhieuGiamGia() != null
                                ? hoaDon.getPhieuGiamGia().getTenGiamGia()
                                : null
                )
                .phieuGiamGia(
                        hoaDon.getPhieuGiamGia() != null
                                ? mapToPhieuGiamGiaResponse(hoaDon.getPhieuGiamGia())
                                : null
                )

                .listIdBan(listIdBan)
                .listTenBan(listTenBan)
                .soHoaDonDangHoatDong(soHoaDonDangHoatDong)
                .coNhieuHoaDon(coNhieuHoaDon)
                .laBanDungChungNhieuHoaDon(coNhieuHoaDon)
                .build();
    }

    private CheckInResponse getChiTietCheckInSauThaoTac(Long idHoaDon, Long idBanAn, String message) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        BanAn banHienTai = quanLyBanRepository.findById(Math.toIntExact(idBanAn))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn"));

        List<HoaDonBanAn> danhSachBan = hoaDonBanAnRepository.findByHoaDon_Id(idHoaDon);

        List<Integer> listIdBan = danhSachBan.stream()
                .map(item -> item.getBanAn().getId())
                .distinct()
                .toList();

        List<String> listTenBan = danhSachBan.stream()
                .map(item -> item.getBanAn().getTenBanAn())
                .distinct()
                .toList();

        CheckInResponse response = new CheckInResponse();

        response.setIdHoaDon(hoaDon.getId());
        response.setMaHoaDon(hoaDon.getMaHoaDon());
        response.setTrangThaiHoaDon(hoaDon.getTrangThai());

        response.setIdBanAn(banHienTai.getId());
        response.setMaBanAn(banHienTai.getMaBanAn());
        response.setTenBanAn(banHienTai.getTenBanAn());
        response.setSucChuaToiDa(banHienTai.getSucChuaToiDa());
        response.setTrangThaiBan(banHienTai.getTrangThai());

        if (banHienTai.getKhuVuc() != null) {
            response.setKhuVucId(banHienTai.getKhuVuc().getId());
            response.setTenKhuVuc(banHienTai.getKhuVuc().getTenKhuVuc());
        }

        if (hoaDon.getDatBan() != null) {
            response.setIdDatBan(hoaDon.getDatBan().getId());
            response.setMaDatBan(hoaDon.getDatBan().getMaDatBan());
            response.setHinhThucDat(hoaDon.getDatBan().getHinhThucDat());
        }

        if (hoaDon.getKhachHang() != null) {
            response.setTenKhachHang(hoaDon.getKhachHang().getTenKhachHang());
            response.setSoDienThoai(hoaDon.getKhachHang().getSoDienThoai());
        }

        response.setListIdBan(listIdBan);
        response.setListTenBan(listTenBan);

        response.setMessage(message);

        return response;
    }

    @Override
    public List<BanAnTrongResponse> getBanTrong() {

        return quanLyBanRepository.findAllBanTrong();
    }


    private List<Integer> layListIdBanTheoHoaDon(Long idHoaDon) {
        if (idHoaDon == null) return List.of();

        return hoaDonBanAnRepository.findByHoaDon_Id(idHoaDon)
                .stream()
                .map(item -> item.getBanAn().getId())
                .distinct()
                .toList();
    }

    private List<String> layListTenBanTheoHoaDon(Long idHoaDon) {
        if (idHoaDon == null) return List.of();

        return hoaDonBanAnRepository.findByHoaDon_Id(idHoaDon)
                .stream()
                .map(item -> item.getBanAn().getTenBanAn())
                .distinct()
                .toList();
    }

    private String layTenTangTheoHoaDon(Long idHoaDon) {
        if (idHoaDon == null) return null;

        return hoaDonBanAnRepository.findByHoaDon_Id(idHoaDon)
                .stream()
                .filter(item -> item.getBanAn() != null && item.getBanAn().getKhuVuc() != null)
                .map(item -> item.getBanAn().getKhuVuc().getTenKhuVuc())
                .findFirst()
                .orElse(null);
    }

    // tạo hóa đơn vào bàn đang dùng
    @Override
    @Transactional
    public CheckInResponse taoHoaDonMoiTrenBan(TaoHoaDonMoiTrenBanRequest request) {
        if (request == null) {
            throw new RuntimeException("Request không được để null");
        }

        if (request.getIdBanAn() == null) {
            throw new RuntimeException("Thiếu id bàn ăn");
        }

        if (request.getIdNhanVien() == null) {
            throw new RuntimeException("Thiếu id nhân viên");
        }

        if (request.getSoKhach() == null || request.getSoKhach() <= 0) {
            throw new RuntimeException("Vui lòng nhập số khách hợp lệ");
        }

        BanAn banAn = quanLyBanRepository.findById(request.getIdBanAn())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn ăn"));

        // KHOÁ BÀN LẠI DƯỚI DB (Tránh 2 nhân viên cùng tạo hóa đơn mới 1 lúc dẫn đến lố số khách)
        banAn.setNgayCapNhat(DateTimeUtils.now());
        quanLyBanRepository.saveAndFlush(banAn);

        if (!Objects.equals(banAn.getTrangThai(), TrangThaiBan.DANG_SU_DUNG.getValue())) {
            throw new RuntimeException("Chỉ tạo hóa đơn mới trên bàn đang sử dụng");
        }

        NhanVien nhanVien = nhanVienRepository.findById(request.getIdNhanVien())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        List<Integer> trangThaiDangHoatDong = List.of(
                TrangThaiHoaDon.VUA_TAO.getCode(),
                TrangThaiHoaDon.CHO_COC.getCode(),
                TrangThaiHoaDon.DA_COC.getCode(),
                TrangThaiHoaDon.DA_XAC_NHAN.getCode(),
                TrangThaiHoaDon.KHACH_DEN.getCode(),
                TrangThaiHoaDon.DANG_PHUC_VU.getCode(),
                TrangThaiHoaDon.CHO_THANH_TOAN.getCode()
        );

        List<HoaDonBanAn> dsLienKetDangHoatDong = hoaDonBanAnRepository.findHoaDonDangHoatDongByBan(
                request.getIdBanAn(),
                trangThaiDangHoatDong
        );
        int soKhachDangDung = dsLienKetDangHoatDong.stream()
                .mapToInt(lienKet -> lienKet.getSoKhach() != null ? lienKet.getSoKhach() : 0)
                .sum();


        if (soKhachDangDung + request.getSoKhach() > banAn.getSucChuaToiDa()) {
            throw new RuntimeException(
                    "Bàn không đủ chỗ. Hiện có " + soKhachDangDung
                            + " khách, thêm " + request.getSoKhach()
                            + " sẽ vượt sức chứa " + banAn.getSucChuaToiDa()
            );
        }

        KhachHang khachVangLai = new KhachHang();
        khachVangLai.setTenKhachHang("Khách vãng lai");
        khachVangLai.setMatKhau("0000000");
        khachVangLai.setEmail("khachvanglai" + UUID.randomUUID().toString().substring(0, 8) + "@gmail.com");
        khachVangLai.setSoDienThoai(null);
        khachVangLai.setNgaySinh(null);
        khachVangLai.setTrangThai(1);
        khachVangLai = khachHangRepository.saveAndFlush(khachVangLai);

        DatBan datBan = DatBan.builder()
                .khachHang(khachVangLai)
                .banAns(new ArrayList<>(List.of(banAn)))
                .maDatBan(generateMaDatBanQuay())
                .thoiGianDat(DateTimeUtils.now())
                .soNguoiToiDa(request.getSoKhach())
                .ghiChu(
                        request.getGhiChu() != null && !request.getGhiChu().trim().isEmpty()
                                ? request.getGhiChu().trim()
                                : "Tạo hóa đơn mới trên bàn đang sử dụng"
                )
                .hinhThucDat(0)
                .trangThai(TrangThaiDatBan.DANG_PHUC_VU.getCode())
                .tienCoc(BigDecimal.ZERO)
                .phanTramCoc(BigDecimal.ZERO)
                .hanGiuBan(null)
                .hanThanhToanCoc(null)
                .trangThaiCoc("KHONG_CAN_COC")
                .build();

        datBan = datBanRepository.saveAndFlush(datBan);

        HoaDon hoaDon = HoaDon.builder()
                .maHoaDon(generateMaHoaDon())
                .loaiHoaDon(1)
                .thoiGianTao(DateTimeUtils.now())
                .thoiGianMo(DateTimeUtils.now())
                .ngayCapNhat(DateTimeUtils.now())
                .trangThai(TrangThaiHoaDon.DANG_PHUC_VU.getCode())
                .ghiChu(request.getGhiChu() != null ? request.getGhiChu().trim() : null)
                .nhanVien(nhanVien)
                .khachHang(khachVangLai)
                .datBan(datBan)
                .tongTien(BigDecimal.ZERO)
                .tongTienMon(BigDecimal.ZERO)
                .tienSauGiam(BigDecimal.ZERO)
                .soTienDuocGiam(BigDecimal.ZERO)
                .phiVanChuyen(BigDecimal.ZERO)
                .tienThue(BigDecimal.ZERO)
                .tienCoc(BigDecimal.ZERO)
                .tongTienDaThanhToan(BigDecimal.ZERO)
                .build();

        hoaDon = hoaDonRepository.saveAndFlush(hoaDon);

        hoaDonBanAnRepository.save(
                HoaDonBanAn.builder()
                        .hoaDon(hoaDon)
                        .banAn(banAn)
                        .soKhach(request.getSoKhach())
                        .build()
        );

        lichSuHoaDonRepository.save(
                LichSuHoaDon.builder()
                        .hoaDon(hoaDon)
                        .thoiGian(DateTimeUtils.now())
                        .ngayTao(DateTimeUtils.now())
                        .ngayCapNhat(DateTimeUtils.now())
                        .hanhDong("TAO_HOA_DON_MOI_TREN_BAN")
                        .trangThai(1)
                        .ghiChu("Tạo hóa đơn mới trên bàn đang sử dụng, số khách: " + request.getSoKhach())
                        .nhanVien(nhanVien)
                        .build()
        );

        return CheckInResponse.builder()
                .idBanAn(banAn.getId())
                .maBanAn(banAn.getMaBanAn())
                .tenBanAn(banAn.getTenBanAn())
                .sucChuaToiDa(banAn.getSucChuaToiDa())
                .trangThaiBan(TrangThaiBan.DANG_SU_DUNG.getValue())
                .khuVucId(banAn.getKhuVuc() != null ? banAn.getKhuVuc().getId() : null)
                .tenKhuVuc(banAn.getKhuVuc() != null ? banAn.getKhuVuc().getTenKhuVuc() : null)
                .idDatBan(datBan.getId())
                .maDatBan(datBan.getMaDatBan())
                .hinhThucDat(datBan.getHinhThucDat())
                .tenKhachHang(khachVangLai.getTenKhachHang())
                .soDienThoai(khachVangLai.getSoDienThoai())
                .thoiGianDat(datBan.getThoiGianDat())
                .soNguoiToiDa(datBan.getSoNguoiToiDa())
                .trangThaiDatBan(datBan.getTrangThai())
                .ghiChuDatBan(datBan.getGhiChu())
                .idHoaDon(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .trangThaiHoaDon(hoaDon.getTrangThai())
                .message("Tạo hóa đơn mới trên bàn thành công")
                .build();
    }

    private DatBanResponse mapToDatBanResponse(DatBan db) {
        if (db == null) return null;

        List<Integer> listIdBan = db.getBanAns() != null ? db.getBanAns().stream().map(BanAn::getId).toList() : null;
        List<String> listTenBan = db.getBanAns() != null ? db.getBanAns().stream().map(BanAn::getTenBanAn).toList() : null;
        String tenKhuVuc = (db.getBanAns() != null && !db.getBanAns().isEmpty() && db.getBanAns().get(0).getKhuVuc() != null)
                ? db.getBanAns().get(0).getKhuVuc().getTenKhuVuc()
                : null;

        return DatBanResponse.builder()
                .id(db.getId())
                .maDatBan(db.getMaDatBan())
                .idKhachHang(db.getKhachHang() != null ? db.getKhachHang().getId() : null)
                .maKhachHang(db.getKhachHang() != null ? db.getKhachHang().getMaKhachHang() : null)
                .tenKhachHang(db.getKhachHang() != null ? db.getKhachHang().getTenKhachHang() : "Khách vãng lai")
                .soDienThoai(db.getKhachHang() != null ? db.getKhachHang().getSoDienThoai() : null)
                .listIdBan(listIdBan)
                .listTenBan(listTenBan)
                .tenKhuVuc(tenKhuVuc)
                .thoiGianDat(db.getThoiGianDat())
                .soNguoiToiDa(db.getSoNguoiToiDa())
                .ghiChu(db.getGhiChu())
                .hinhThucDat(db.getHinhThucDat())
                .trangThai(db.getTrangThai())
                .ngayTao(db.getNgayTao())
                .ngayCapNhat(db.getNgayCapNhat())
                .tienCoc(db.getTienCoc())
                .phanTramCoc(db.getPhanTramCoc())
                .hanGiuBan(db.getHanGiuBan())
                .hanThanhToanCoc(db.getHanThanhToanCoc())
                .trangThaiCoc(db.getTrangThaiCoc())
                .build();
    }

    @Override
    public List<DatBanResponse> searchDatBan(String keyword) {
        List<DatBan> list = datBanRepository.searchDatBanByKeyword(keyword);
        return list.stream().map(this::mapToDatBanResponse).toList();
    }

    public byte[] exportPdfGoiMon(Long idHoaDon, List<Long> chiTietIds) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        HoaDonResponse hoaDonResponse = mapToHoaDonResponse(hoaDon);

        List<HoaDonChiTiet> dsChiTiet = hoaDonChiTietRepository.findAllByHoaDon_Id(idHoaDon).stream()
                .filter(ct -> chiTietIds != null && !chiTietIds.isEmpty() ? chiTietIds.contains(ct.getId()) : (ct.getTrangThai() == null || ct.getTrangThai() == 0))
                .toList();
        
        int soKhach = hoaDonBanAnRepository.findByHoaDon_Id(idHoaDon).stream()
                .mapToInt(lk -> lk.getSoKhach() != null ? lk.getSoKhach() : 0)
                .sum();
        if (soKhach == 0 && hoaDon.getDatBan() != null && hoaDon.getDatBan().getSoNguoiToiDa() != null) {
            soKhach = hoaDon.getDatBan().getSoNguoiToiDa();
        }

        return goiMonCheckInPdf.generateGoiMonPdf(hoaDonResponse, dsChiTiet, soKhach);
    }

    @Override
    public boolean xacThucMaDatBan(Long idDatBan, String maDatBan) {
        if (idDatBan == null || maDatBan == null) {
            return false;
        }
        DatBan datBan = datBanRepository.findById(idDatBan)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));
        return datBan.getMaDatBan().equalsIgnoreCase(maDatBan.trim());
    }

    @Override
    @Transactional
    public void autoDonBan() {
        Integer thoiGianDonBan = thamSoHeThongService.getInt(ThamSoHeThong.THOI_GIAN_DON_BAN.getCode());

        if (thoiGianDonBan == null || thoiGianDonBan <= 0) {
            thoiGianDonBan = 5;
        }

        LocalDateTime mocThoiGian = DateTimeUtils.now()
                .minusMinutes(thoiGianDonBan);

        List<BanAn> dsBanDangDon =
                quanLyBanRepository.findByTrangThai(
                        TrangThaiBan.DANG_DON.getValue()
                );

        for (BanAn ban : dsBanDangDon) {
            if (ban.getNgayCapNhat() != null && ban.getNgayCapNhat().isBefore(mocThoiGian)) {
                ban.setTrangThai(TrangThaiBan.TRONG.getValue());
                ban.setNgayCapNhat(DateTimeUtils.now());
                quanLyBanRepository.save(ban);  

                webSocketService.sendTopic("/topic/ban", "update");

                log.info(
                        "[AUTO_DON_BAN] Ban {} -> TRONG",
                        ban.getTenBanAn()
                );
            }
        }
    }

    @Override
    public List<DatBanResponse> findByNgay(SearchKhachChoCheckInRequest request) {


        List<DatBan> list = datBanRepository.findAllByNgay(request);

        return list.stream()
                .map(datBanMapper::mapToResponseHienThi)
                .toList();
    }
}