package com.fourspicehotpot.backend.modules.checkin.service.serviceImpl;

import com.fourspicehotpot.backend.common.enums.LoaiThanhToan;
import com.fourspicehotpot.backend.common.enums.ThamSoHeThong;
import com.fourspicehotpot.backend.common.enums.TrangThaiBan;
import com.fourspicehotpot.backend.common.enums.TrangThaiDatBan;
import com.fourspicehotpot.backend.common.enums.TrangThaiHoaDon;
import com.fourspicehotpot.backend.common.util.DateTimeUtils;
import com.fourspicehotpot.backend.integration.payment.vnpay.service.VNPayService;
import com.fourspicehotpot.backend.integration.payment.zalopay.service.ZaloPayService;
import com.fourspicehotpot.backend.integration.websocket.service.WebSocketService;
import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.banan.repository.BanAnRepository;
import com.fourspicehotpot.backend.modules.checkin.dto.request.TaoThanhToanRequest;
import com.fourspicehotpot.backend.modules.checkin.dto.response.HoaDonThanhToanItemResponse;
import com.fourspicehotpot.backend.modules.checkin.dto.response.HoaDonThanhToanResponse;
import com.fourspicehotpot.backend.modules.checkin.dto.response.TaoThanhToanResponse;
import com.fourspicehotpot.backend.modules.checkin.dto.response.ThanhToanResponse;
import com.fourspicehotpot.backend.modules.checkin.entity.PhuongThucThanhToan;
import com.fourspicehotpot.backend.modules.checkin.entity.ThanhToan;
import com.fourspicehotpot.backend.modules.checkin.repository.PhuongThucThanhToanRepository;
import com.fourspicehotpot.backend.modules.checkin.repository.ThanhToanRepository;
import com.fourspicehotpot.backend.modules.checkin.service.ThanhToanService;
import com.fourspicehotpot.backend.modules.datban.entity.DatBan;
import com.fourspicehotpot.backend.modules.datban.repository.DatBanRepository;
import com.fourspicehotpot.backend.modules.hoadon.entity.*;
import com.fourspicehotpot.backend.modules.hoadon.repository.*;
import com.fourspicehotpot.backend.modules.thamsohethong.service.ThamSoHeThongService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.swing.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ThanhToanServiceImpl implements ThanhToanService {

    @Autowired
    private ThanhToanRepository thanhToanRepository;

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @Autowired
    private LichSuThanhToanRepository lichSuThanhToanRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    private BanAnRepository quanLyBanRepository;

    @Autowired
    private DatBanRepository datBanRepository;

    @Autowired
    private HoaDonBanAnRepository hoaDonBanAnRepository;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private ZaloPayService zaloPayService;

    @Autowired
    private ThamSoHeThongService thamSoHeThongService;

    @Autowired
    private WebSocketService webSocketService;

    @Override
    public List<PhuongThucThanhToan> getAllPhuongThucThanhToan() {
        return phuongThucThanhToanRepository.findAllActive();
    }

    @Override
    @Transactional
    public TaoThanhToanResponse thanhToanTienMat(TaoThanhToanRequest request) {
        return xuLyThanhToanNoiBo(request, "Tiền mặt");
    }

    private TaoThanhToanResponse xuLyThanhToanNoiBo(TaoThanhToanRequest request, String ghiChuMacDinh) {
        if (request.getIdHoaDon() == null) {
            throw new RuntimeException("Thiếu id hóa đơn");
        }

        if (request.getIdPhuongThucThanhToan() == null) {
            throw new RuntimeException("Thiếu phương thức thanh toán");
        }

        if (request.getSoTien() == null || request.getSoTien().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền thanh toán phải lớn hơn 0");
        }

        HoaDon hoaDon = hoaDonRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (hoaDon.getTrangThai() != null
                && hoaDon.getTrangThai().equals(TrangThaiHoaDon.DA_HUY.getCode())) {
            throw new RuntimeException("Hóa đơn đã hủy");
        }

        PhuongThucThanhToan phuongThuc = phuongThucThanhToanRepository
                .findById(request.getIdPhuongThucThanhToan())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức thanh toán"));

        if (phuongThuc.getTrangThai() == null || phuongThuc.getTrangThai() != 1) {
            throw new RuntimeException("Phương thức thanh toán không hoạt động");
        }

        if (hoaDon.getTrangThai() == null
                || hoaDon.getTrangThai() < TrangThaiHoaDon.CHO_THANH_TOAN.getCode()) {
            hoaDon.setTrangThai(TrangThaiHoaDon.CHO_THANH_TOAN.getCode());
            hoaDon.setNgayCapNhat(DateTimeUtils.now());
            hoaDonRepository.save(hoaDon);
        }

        BigDecimal tongPhaiTra = tinhTongPhaiTraChoOnline(hoaDon);

        BigDecimal tongDaThanhToan = thanhToanRepository.tongDaThanhToan(hoaDon.getId());
        if (tongDaThanhToan == null) {
            tongDaThanhToan = BigDecimal.ZERO;
        }

        BigDecimal conLai = tongPhaiTra.subtract(tongDaThanhToan);

        if (conLai.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Hóa đơn đã được thanh toán đủ");
        }

        BigDecimal soTienThanhToan = request.getSoTien();
        if (soTienThanhToan.compareTo(conLai) > 0) {
            soTienThanhToan = conLai;
        }

        String maThanhToan = "TT-" + System.currentTimeMillis();

        ThanhToan thanhToan = ThanhToan.builder()
                .hoaDon(hoaDon)
                .phuongThucThanhToan(phuongThuc)
                .maThanhToan(maThanhToan)
                .tongTien(soTienThanhToan)
                .trangThai(1)
                .ngayThanhToan(DateTimeUtils.now())
                .ngayCapNhat(DateTimeUtils.now())
                .build();

        thanhToan = thanhToanRepository.save(thanhToan);

        LichSuThanhToan lichSu = LichSuThanhToan.builder()
                .hoaDon(hoaDon)
                .maGiaoDich(maThanhToan)
                .soTien(soTienThanhToan)
                .phuongThuc(phuongThuc.getTenPhuongThuc())
                .trangThai(1)
                .loaiThanhToan(LoaiThanhToan.THANH_TOAN_MON)
                .thoiGian(DateTimeUtils.now())
                .ghiChu(request.getGhiChu() != null && !request.getGhiChu().isBlank()
                        ? request.getGhiChu()
                        : ghiChuMacDinh)
                .build();

        lichSuThanhToanRepository.save(lichSu);

        luuLichSuHoaDonThanhToan(
                hoaDon,
                phuongThuc,
                soTienThanhToan,
                request.getGhiChu()
        );

        BigDecimal tongSauThanhToan = thanhToanRepository.tongDaThanhToan(hoaDon.getId());
        if (tongSauThanhToan == null) {
            tongSauThanhToan = BigDecimal.ZERO;
        }

        hoaDon.setTongTienDaThanhToan(tongSauThanhToan);

        if (tongSauThanhToan.compareTo(tongPhaiTra) >= 0) {
            capNhatSauKhiThanhToanDu(hoaDon);
        } else {
            BigDecimal conLaiMoi = tongPhaiTra.subtract(tongSauThanhToan);
            if (conLaiMoi.compareTo(BigDecimal.ZERO) < 0) {
                conLaiMoi = BigDecimal.ZERO;
            }

            hoaDon.setTrangThai(TrangThaiHoaDon.CHO_THANH_TOAN.getCode());
            hoaDon.setNgayCapNhat(DateTimeUtils.now());
            hoaDon.setTienConLai(conLaiMoi);
            hoaDonRepository.save(hoaDon);
        }

        return new TaoThanhToanResponse(
                thanhToan.getId(),
                thanhToan.getMaThanhToan(),
                null,
                null,
                thanhToan.getTrangThai(),
                "Thanh toán thành công"
        );
    }

    @Override
    @Transactional
    public TaoThanhToanResponse thanhToanChuyenKhoan(TaoThanhToanRequest request) {
        if (request.getIdHoaDon() == null) {
            throw new RuntimeException("Thiếu id hóa đơn");
        }

        if (request.getIdPhuongThucThanhToan() == null) {
            throw new RuntimeException("Thiếu phương thức thanh toán");
        }

        if (request.getSoTien() == null || request.getSoTien().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền phải lớn hơn 0");
        }

        HoaDon hoaDon = hoaDonRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        PhuongThucThanhToan phuongThuc = phuongThucThanhToanRepository
                .findById(request.getIdPhuongThucThanhToan())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức thanh toán"));

        if (phuongThuc.getTrangThai() == null || phuongThuc.getTrangThai() != 1) {
            throw new RuntimeException("Phương thức thanh toán không hoạt động");
        }

        if (hoaDon.getTrangThai() == null
                || hoaDon.getTrangThai() < TrangThaiHoaDon.CHO_THANH_TOAN.getCode()) {
            hoaDon.setTrangThai(TrangThaiHoaDon.CHO_THANH_TOAN.getCode());
            hoaDon.setNgayCapNhat(DateTimeUtils.now());
            hoaDonRepository.save(hoaDon);
        }

        BigDecimal tongPhaiTra = tinhTongPhaiTraChoOnline(hoaDon);

        BigDecimal tongDaThanhToan = thanhToanRepository.tongDaThanhToan(hoaDon.getId());
        if (tongDaThanhToan == null) {
            tongDaThanhToan = BigDecimal.ZERO;
        }

        BigDecimal conLai = tongPhaiTra.subtract(tongDaThanhToan);
        if (conLai.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Hóa đơn này đã được thanh toán đủ");
        }

        BigDecimal soTienHopLe = request.getSoTien();
        if (soTienHopLe.compareTo(conLai) > 0) {
            soTienHopLe = conLai;
        }

        String tenPhuongThuc = phuongThuc.getTenPhuongThuc() != null
                ? phuongThuc.getTenPhuongThuc().trim().toLowerCase()
                : "";

        if (tenPhuongThuc.contains("chuyển khoản")) {
            return xuLyChuyenKhoanThuCong(request, hoaDon, phuongThuc);
        }

        String maThanhToan = "TT-" + System.currentTimeMillis();

        ThanhToan thanhToan = ThanhToan.builder()
                .hoaDon(hoaDon)
                .phuongThucThanhToan(phuongThuc)
                .maThanhToan(maThanhToan)
                .tongTien(soTienHopLe)
                .trangThai(0)
                .ngayThanhToan(DateTimeUtils.now())
                .ngayCapNhat(DateTimeUtils.now())
                .build();

        thanhToan = thanhToanRepository.save(thanhToan);

        LichSuThanhToan lichSu = LichSuThanhToan.builder()
                .hoaDon(hoaDon)
                .maGiaoDich(maThanhToan)
                .soTien(soTienHopLe)
                .phuongThuc(phuongThuc.getTenPhuongThuc())
                .trangThai(0)
                .loaiThanhToan(LoaiThanhToan.THANH_TOAN_MON)
                .thoiGian(DateTimeUtils.now())
                .ghiChu(request.getGhiChu() != null ? request.getGhiChu() : "Khởi tạo thanh toán online")
                .build();

        lichSuThanhToanRepository.save(lichSu);

        if (tenPhuongThuc.contains("vnpay")) {
            HttpServletRequest httpRequest =
                    ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

            String paymentUrl = vnPayService.createInvoicePaymentUrl(
                    soTienHopLe.setScale(0, java.math.RoundingMode.HALF_UP).longValue(),
                    maThanhToan,
                    httpRequest
            );

            return new TaoThanhToanResponse(
                    thanhToan.getId(),
                    thanhToan.getMaThanhToan(),
                    paymentUrl,
                    null,
                    thanhToan.getTrangThai(),
                    "Tạo link VNPAY thành công"
            );
        }

        if (tenPhuongThuc.contains("zalopay")) {
            Map<String, Object> zaloResponse = zaloPayService.createInvoiceOrder(
                    maThanhToan,
                    soTienHopLe
            );

            return new TaoThanhToanResponse(
                    thanhToan.getId(),
                    thanhToan.getMaThanhToan(),
                    (String) zaloResponse.get("order_url"),
                    (String) zaloResponse.get("qr_code"),
                    thanhToan.getTrangThai(),
                    "Tạo link ZaloPay thành công"
            );
        }

        throw new RuntimeException("Hiện tại chỉ hỗ trợ Chuyển khoản thường, VNPAY và ZaloPay");
    }

    @Override
    @Transactional
    public void xacNhanThanhToanThanhCong(String maThanhToan) {
        ThanhToan thanhToan = thanhToanRepository.findByMaThanhToan(maThanhToan)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã thanh toán"));

        if (thanhToan.getTrangThai() != null && thanhToan.getTrangThai() == 1) {
            return;
        }

        thanhToan.setTrangThai(1);
        thanhToan.setNgayCapNhat(DateTimeUtils.now());
        thanhToanRepository.save(thanhToan);

        HoaDon hoaDon = thanhToan.getHoaDon();

        LichSuThanhToan lichSu = LichSuThanhToan.builder()
                .hoaDon(hoaDon)
                .maGiaoDich(maThanhToan)
                .soTien(thanhToan.getTongTien())
                .phuongThuc(thanhToan.getPhuongThucThanhToan().getTenPhuongThuc())
                .trangThai(1)
                .loaiThanhToan(LoaiThanhToan.THANH_TOAN_MON)
                .thoiGian(DateTimeUtils.now())
                .ghiChu("Thanh toán online thành công")
                .build();

        lichSuThanhToanRepository.save(lichSu);

        luuLichSuHoaDonThanhToan(
                hoaDon,
                thanhToan.getPhuongThucThanhToan(),
                thanhToan.getTongTien(),
                "Thanh toán online thành công"
        );

        BigDecimal tongPhaiTra = tinhTongPhaiTraChoOnline(hoaDon);

        BigDecimal tongDaThanhToan = thanhToanRepository.tongDaThanhToan(hoaDon.getId());
        if (tongDaThanhToan == null) {
            tongDaThanhToan = BigDecimal.ZERO;
        }

        hoaDon.setTongTienDaThanhToan(tongDaThanhToan);

        if (tongDaThanhToan.compareTo(tongPhaiTra) >= 0) {
            capNhatSauKhiThanhToanDu(hoaDon);
        } else {
            BigDecimal conLaiMoi = tongPhaiTra.subtract(tongDaThanhToan);
            if (conLaiMoi.compareTo(BigDecimal.ZERO) < 0) {
                conLaiMoi = BigDecimal.ZERO;
            }

            hoaDon.setTrangThai(TrangThaiHoaDon.CHO_THANH_TOAN.getCode());
            hoaDon.setNgayCapNhat(DateTimeUtils.now());
            hoaDon.setTienConLai(conLaiMoi);
            hoaDonRepository.save(hoaDon);
        }
    }

    @Override
    @Transactional
    public void xacNhanHoanTatHoaDon(Long idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (hoaDon.getTrangThai() == null
                || !hoaDon.getTrangThai().equals(TrangThaiHoaDon.HOAN_THANH.getCode())) {
            throw new RuntimeException("Hóa đơn chưa ở trạng thái đã thanh toán");
        }

        capNhatSauKhiThanhToanDu(hoaDon);
    }

    @Override
    public List<ThanhToanResponse> getDanhSachThanhToanTheoHoaDon(Long idHoaDon) {
        return thanhToanRepository.findByHoaDon(idHoaDon).stream()
                .map(tt -> new ThanhToanResponse(
                        tt.getId(),
                        tt.getMaThanhToan(),
                        tt.getPhuongThucThanhToan().getId(),
                        tt.getPhuongThucThanhToan().getMaPhuongThuc(),
                        tt.getPhuongThucThanhToan().getTenPhuongThuc(),
                        tt.getTongTien(),
                        tt.getNgayThanhToan(),
                        tt.getTrangThai()
                ))
                .toList();
    }

    @Override
    public HoaDonThanhToanResponse getChiTietHoaDonThanhToan(Long idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        List<HoaDonThanhToanItemResponse> dsMon = hoaDonChiTietRepository.findAllByHoaDon_Id(idHoaDon)
                .stream()
                .filter(item -> item.getSanPham() != null)
                .collect(Collectors.groupingBy(item -> item.getSanPham().getId()))
                .values()
                .stream()
                .map(list -> {
                    HoaDonChiTiet first = list.get(0);

                    Integer tongSoLuong = list.stream()
                            .map(HoaDonChiTiet::getSoLuong)
                            .filter(Objects::nonNull)
                            .reduce(0, Integer::sum);

                    BigDecimal tongThanhTien = list.stream()
                            .map(ct -> ct.getThanhTien() != null ? ct.getThanhTien() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal giaGoc = (first.getSanPham() != null && first.getSanPham().getGia() != null)
                            ? first.getSanPham().getGia()
                            : first.getDonGia();

                    return HoaDonThanhToanItemResponse.builder()
                            .id(first.getId())
                            .maSanPham(first.getSanPham().getMaSanPham())
                            .tenSanPham(first.getSanPham().getTenSanPham())
                            .soLuong(tongSoLuong)
                            .donGia(first.getDonGia())
                            .thanhTien(tongThanhTien)
                            .trangThai(first.getTrangThai())
                            .build();
                })
                .toList();

        String tenBanAn = null;
        String tenKhuVuc = null;

        if (hoaDon.getBanAns() != null && !hoaDon.getBanAns().isEmpty()) {
            BanAn ban = hoaDon.getBanAns().get(0);
            tenBanAn = ban.getTenBanAn();
            tenKhuVuc = ban.getKhuVuc() != null ? ban.getKhuVuc().getTenKhuVuc() : null;
        }

        return HoaDonThanhToanResponse.builder()
                .idHoaDon(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .tenBanAn(tenBanAn)
                .tenKhuVuc(tenKhuVuc)
                .tenKhachHang(hoaDon.getKhachHang() != null
                        ? hoaDon.getKhachHang().getTenKhachHang()
                        : "Khách lẻ")
                .thoiGianTao(hoaDon.getThoiGianTao())
                .tienCoc(hoaDon.getTienCoc() != null ? hoaDon.getTienCoc() : BigDecimal.ZERO)
                .tongTienMon(hoaDon.getTongTienMon() != null ? hoaDon.getTongTienMon() : BigDecimal.ZERO)
                .soTienDuocGiam(hoaDon.getSoTienDuocGiam() != null ? hoaDon.getSoTienDuocGiam() : BigDecimal.ZERO)
                .phiThuKhac(hoaDon.getPhiVanChuyen() != null ? hoaDon.getPhiVanChuyen() : BigDecimal.ZERO)
                .tongTien(hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO)
                .tongTienDaThanhToan(hoaDon.getTongTienDaThanhToan() != null ? hoaDon.getTongTienDaThanhToan() : BigDecimal.ZERO)
                .dsMon(dsMon)
                .build();
    }

    private TaoThanhToanResponse xuLyChuyenKhoanThuCong(
            TaoThanhToanRequest request,
            HoaDon hoaDon,
            PhuongThucThanhToan phuongThuc
    ) {
        BigDecimal tongPhaiTra = tinhTongPhaiTraChoOnline(hoaDon);

        BigDecimal tongDaThanhToan = thanhToanRepository.tongDaThanhToan(hoaDon.getId());
        if (tongDaThanhToan == null) {
            tongDaThanhToan = BigDecimal.ZERO;
        }

        BigDecimal conLai = tongPhaiTra.subtract(tongDaThanhToan);
        if (conLai.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Hóa đơn đã được thanh toán đủ");
        }

        BigDecimal soTienThanhToan = request.getSoTien();
        if (soTienThanhToan.compareTo(conLai) > 0) {
            soTienThanhToan = conLai;
        }

        String maThanhToan = "TT-" + System.currentTimeMillis();

        ThanhToan thanhToan = ThanhToan.builder()
                .hoaDon(hoaDon)
                .phuongThucThanhToan(phuongThuc)
                .maThanhToan(maThanhToan)
                .tongTien(soTienThanhToan)
                .trangThai(1)
                .ngayThanhToan(DateTimeUtils.now())
                .ngayCapNhat(DateTimeUtils.now())
                .build();

        thanhToan = thanhToanRepository.save(thanhToan);

        LichSuThanhToan lichSu = LichSuThanhToan.builder()
                .hoaDon(hoaDon)
                .maGiaoDich(maThanhToan)
                .soTien(soTienThanhToan)
                .phuongThuc(phuongThuc.getTenPhuongThuc())
                .trangThai(1)
                .loaiThanhToan(LoaiThanhToan.THANH_TOAN_MON)
                .thoiGian(DateTimeUtils.now())
                .ghiChu(request.getGhiChu() != null && !request.getGhiChu().isBlank()
                        ? request.getGhiChu()
                        : "Chuyển khoản ngân hàng")
                .build();

        lichSuThanhToanRepository.save(lichSu);

        luuLichSuHoaDonThanhToan(
                hoaDon,
                phuongThuc,
                soTienThanhToan,
                request.getGhiChu()
        );

        BigDecimal tongSauThanhToan = thanhToanRepository.tongDaThanhToan(hoaDon.getId());
        if (tongSauThanhToan == null) {
            tongSauThanhToan = BigDecimal.ZERO;
        }

        hoaDon.setTongTienDaThanhToan(tongSauThanhToan);

        if (tongSauThanhToan.compareTo(tongPhaiTra) >= 0) {
            capNhatSauKhiThanhToanDu(hoaDon);
        } else {
            BigDecimal conLaiMoi = tongPhaiTra.subtract(tongSauThanhToan);
            if (conLaiMoi.compareTo(BigDecimal.ZERO) < 0) {
                conLaiMoi = BigDecimal.ZERO;
            }

            hoaDon.setTrangThai(TrangThaiHoaDon.CHO_THANH_TOAN.getCode());
            hoaDon.setNgayCapNhat(DateTimeUtils.now());
            hoaDon.setTienConLai(conLaiMoi);
            hoaDonRepository.save(hoaDon);
        }

        return new TaoThanhToanResponse(
                thanhToan.getId(),
                thanhToan.getMaThanhToan(),
                null,
                null,
                thanhToan.getTrangThai(),
                "Chuyển khoản ngân hàng thành công"
        );
    }

    private BigDecimal tinhTongPhaiTraChoOnline(HoaDon hoaDon) {
        List<HoaDonChiTiet> dsChiTiet =
                hoaDonChiTietRepository.findAllByHoaDon_Id(hoaDon.getId());

        BigDecimal tongTienMon = dsChiTiet.stream()
                .filter(ct -> ct.getTrangThai() != null && ct.getTrangThai() == 1)
                .map(ct -> ct.getThanhTien() != null ? ct.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal giamGia = hoaDon.getSoTienDuocGiam() != null
                ? hoaDon.getSoTienDuocGiam()
                : BigDecimal.ZERO;

        BigDecimal tienCoc = hoaDon.getTienCoc() != null
                ? hoaDon.getTienCoc()
                : BigDecimal.ZERO;

        BigDecimal tongPhaiTra = tongTienMon.subtract(giamGia).subtract(tienCoc);

        if (tongPhaiTra.compareTo(BigDecimal.ZERO) < 0) {
            tongPhaiTra = BigDecimal.ZERO;
        }

        hoaDon.setTongTienMon(tongTienMon);
        hoaDon.setTongTien(tongPhaiTra);
        hoaDon.setTienConLai(tongPhaiTra);
        hoaDon.setNgayCapNhat(DateTimeUtils.now());
        hoaDonRepository.save(hoaDon);

        return tongPhaiTra;
    }

    private void tuDongDonBan(BanAn ban, HoaDon hoaDon) {
        ban.setTrangThai(TrangThaiBan.DANG_DON.getValue());
        ban.setNgayCapNhat(DateTimeUtils.now());
        quanLyBanRepository.save(ban);
        webSocketService.sendTopic("/topic/ban", "update");
    }

    private void capNhatSauKhiThanhToanDu(HoaDon hoaDon) {
        LocalDateTime now = DateTimeUtils.now();

        boolean daThanhToanTruocDo = hoaDon.getTrangThai() != null
                && hoaDon.getTrangThai().equals(TrangThaiHoaDon.HOAN_THANH.getCode());

        hoaDon.setTrangThai(TrangThaiHoaDon.HOAN_THANH.getCode());
        hoaDon.setThoiGianThanhToan(
                hoaDon.getThoiGianThanhToan() != null
                        ? hoaDon.getThoiGianThanhToan()
                        : now
        );
        hoaDon.setNgayCapNhat(now);
        hoaDon.setTienConLai(BigDecimal.ZERO);
        hoaDonRepository.save(hoaDon);

        if (hoaDon.getDatBan() != null) {
            DatBan datBan = hoaDon.getDatBan();
            datBan.setTrangThai(TrangThaiDatBan.HOAN_THANH.getCode());
            datBan.setNgayCapNhat(now);
            datBanRepository.save(datBan);
        }

        List<Integer> trangThaiHoaDonConMo = List.of(
                TrangThaiHoaDon.VUA_TAO.getCode(),
                TrangThaiHoaDon.CHO_COC.getCode(),
                TrangThaiHoaDon.DA_COC.getCode(),
                TrangThaiHoaDon.DA_XAC_NHAN.getCode(),
                TrangThaiHoaDon.KHACH_DEN.getCode(),
                TrangThaiHoaDon.DANG_PHUC_VU.getCode(),
                TrangThaiHoaDon.CHO_THANH_TOAN.getCode()
        );

        List<HoaDonBanAn> dsLienKet = hoaDonBanAnRepository.findByHoaDon_Id(hoaDon.getId());

        for (HoaDonBanAn lienKet : dsLienKet) {
            BanAn ban = lienKet.getBanAn();
            if (ban == null) continue;

            boolean conHoaDonMoKhac = hoaDonBanAnRepository
                    .findHoaDonDangHoatDongByBan(ban.getId(), trangThaiHoaDonConMo)
                    .stream()
                    .anyMatch(x -> x.getHoaDon() != null
                            && !x.getHoaDon().getId().equals(hoaDon.getId()));

            if (!conHoaDonMoKhac) {
                tuDongDonBan(ban, hoaDon);
            }
        }

        if (!daThanhToanTruocDo) {
            lichSuHoaDonRepository.save(
                    LichSuHoaDon.builder()
                            .hoaDon(hoaDon)
                            .thoiGian(now)
                            .ngayTao(now)
                            .ngayCapNhat(now)
                            .hanhDong("HOAN_TAT_THANH_TOAN")
                            .trangThai(1)
                            .ghiChu("Hóa đơn đã được thanh toán đủ")
                            .build()
            );
        }
    }

    private void luuLichSuHoaDonThanhToan(
            HoaDon hoaDon,
            PhuongThucThanhToan phuongThuc,
            BigDecimal soTien,
            String ghiChuThem
    ) {
        LocalDateTime now = DateTimeUtils.now();

        String tenPhuongThuc = phuongThuc != null && phuongThuc.getTenPhuongThuc() != null
                ? phuongThuc.getTenPhuongThuc()
                : "Không xác định";

        String ghiChu = "Thanh toán bằng " + tenPhuongThuc
                + " - Số tiền: " + soTien;

        if (ghiChuThem != null && !ghiChuThem.isBlank()) {
            ghiChu += " - " + ghiChuThem.trim();
        }

        lichSuHoaDonRepository.save(
                LichSuHoaDon.builder()
                        .hoaDon(hoaDon)
                        .thoiGian(now)
                        .ngayTao(now)
                        .ngayCapNhat(now)
                        .hanhDong("THANH_TOAN")
                        .trangThai(1)
                        .ghiChu(ghiChu)
                        .build()
        );
    }
}