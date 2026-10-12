package com.fourspicehotpot.backend.modules.datban.service.serviceImpl;

import com.fourspicehotpot.backend.common.enums.*;
import com.fourspicehotpot.backend.common.util.DateTimeUtils;
import com.fourspicehotpot.backend.integration.mail.service.MailService;
import com.fourspicehotpot.backend.integration.payment.vnpay.service.VNPayService;
import com.fourspicehotpot.backend.integration.websocket.service.WebSocketService;
import com.fourspicehotpot.backend.modules.banan.repository.BanAnRepository;
import com.fourspicehotpot.backend.modules.datban.dto.request.CustomerDatBanRequest;
import com.fourspicehotpot.backend.modules.datban.entity.DatBan;
import com.fourspicehotpot.backend.modules.datban.repository.DatBanRepository;
import com.fourspicehotpot.backend.modules.datban.service.CustomerDatBanService;
import com.fourspicehotpot.backend.modules.dotgiamgia.entity.DotGiamGia;
import com.fourspicehotpot.backend.modules.dotgiamgia.entity.DotGiamGiaSanPham;
import com.fourspicehotpot.backend.modules.dotgiamgia.repository.DotGiamGiaSanPhamRepository;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDonChiTiet;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonChiTietRepository;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonRepository;
import com.fourspicehotpot.backend.modules.hoadon.service.LichSuHoaDonService;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.khachhang.repository.KhachHangRepository;
import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import com.fourspicehotpot.backend.modules.setlau.repository.SanPhamRepository;
import com.fourspicehotpot.backend.modules.thamsohethong.service.ThamSoHeThongService;
import com.fourspicehotpot.backend.modules.thucdon.dto.request.CustomerMonDatTruocRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerDatBanServiceImpl implements CustomerDatBanService {

    private static final Logger log = LoggerFactory.getLogger(DatBanServiceImpl.class);
    private final DatBanRepository datBanRepository;
    private final KhachHangRepository khachHangRepository;
    private final BanAnRepository banAnRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final HoaDonRepository hoaDonRepository;
    private final SanPhamRepository sanPhamRepository;
    private final LichSuHoaDonService lichSuHoaDonService;
    private final ThamSoHeThongService thamSoHeThongService;
    private final VNPayService vNPayService;
    private final WebSocketService webSocketService;
    private final MailService mailService;
    private final DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;

    /// /// Lãnh Minh Hiếu /// ///

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String datBanOnline(CustomerDatBanRequest request, HttpServletRequest httpRequest) {

        String code = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 4)
                .toUpperCase();
        try {
            Optional<KhachHang> optionalKhachHang =
                    khachHangRepository.findBySoDienThoai(request.getSoDienThoai());
            KhachHang khachHang;
            if (optionalKhachHang.isEmpty()) {
                khachHang = new KhachHang();
                khachHang.setSoDienThoai(request.getSoDienThoai());
                khachHang.setTenKhachHang(request.getTenKhachHang());
                khachHang.setEmail(request.getEmail());
                khachHang.setMatKhau("khachvanglai");
                khachHang = khachHangRepository.save(khachHang);
            } else {
                khachHang = optionalKhachHang.get();
            }

            DatBan datBan = new DatBan();
            int hanGiuBan = thamSoHeThongService.getInt(ThamSoHeThong.THOI_GIAN_GIU_BAN.getCode());
            int hanThanhToanCoc = thamSoHeThongService.getInt(ThamSoHeThong.THOI_GIAN_CHO_THANH_TOAN_COC.getCode());
            datBan.setKhachHang(khachHang);
            datBan.setMaDatBan("DB-" + code);
            datBan.setBanAns(null);
            datBan.setThoiGianDat(request.getThoiGianDat());
            datBan.setSoNguoiToiDa(request.getSoNguoiToiDa());
            datBan.setGhiChu(request.getGhiChu());
            datBan.setHinhThucDat(1);
            datBan.setTrangThai(TrangThaiDatBan.CHO_XAC_NHAN.getCode());
            datBan.setNgayTao(DateTimeUtils.now());
            datBan.setHanGiuBan(DateTimeUtils.now().plusMinutes(hanGiuBan));
            datBan.setHanThanhToanCoc(DateTimeUtils.now().plusMinutes(hanThanhToanCoc));
            DatBan savedDatBan = datBanRepository.save(datBan);
            HoaDon savedHoaDon = null;
            HoaDon hoaDon = new HoaDon();
            String ma = "HD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 7).toUpperCase();
            hoaDon.setMaHoaDon(ma);
            hoaDon.setDatBan(savedDatBan);
            hoaDon.setKhachHang(khachHang);
            hoaDon.setBanAns(null);
            hoaDon.setTrangThai(TrangThaiHoaDon.VUA_TAO.getCode());
            hoaDon.setLoaiHoaDon(2);

            savedHoaDon = hoaDonRepository.save(hoaDon);
            if (request.getMonDatTruoc() != null && !request.getMonDatTruoc().isEmpty()) {
                for (CustomerMonDatTruocRequest mon : request.getMonDatTruoc()) {
                    SanPham sanPham =
                            sanPhamRepository.findById(mon.getIdSanPham())
                                    .orElseThrow(() ->
                                            new RuntimeException("KHONG TIM THAY SAN PHAM: " + mon.getIdSanPham()));
                    HoaDonChiTiet chiTiet = new HoaDonChiTiet();
                    BigDecimal giaSauGiam = tinhGiaSauGiam(sanPham);

                    chiTiet.setHoaDon(savedHoaDon);
                    chiTiet.setSanPham(sanPham);
                    chiTiet.setSoLuong(mon.getSoLuong());
                    chiTiet.setDonGia(giaSauGiam);
                    chiTiet.setThanhTien(giaSauGiam.multiply(BigDecimal.valueOf(mon.getSoLuong())));
                    chiTiet.setTrangThai(0);
                    chiTiet.setNgayTao(DateTimeUtils.now());
                    hoaDonChiTietRepository.save(chiTiet);

                }
                lichSuHoaDonService.saveLichSuHoaDon(
                        savedHoaDon, "DAT_BAN",
                        1,
                        "Khách " + request.getTenKhachHang() + " đặt bàn"

                );
            } else {
                lichSuHoaDonService.saveLichSuHoaDon(
                        savedHoaDon, "DAT_BAN", 1,
                        "Khách " + request.getTenKhachHang() + " đặt bàn"
                );
            }
            BigDecimal tienCoc = null;

            if (request.getMonDatTruoc() != null && !request.getMonDatTruoc().isEmpty()) {

                BigDecimal tongTien = hoaDonChiTietRepository.tinhTongTienByHoaDon(savedHoaDon.getId());
                Double percent = thamSoHeThongService.getDouble(ThamSoHeThong.PHAN_TRAM_COC.getCode());

                tienCoc = tongTien.multiply(BigDecimal.valueOf(percent))
                        .divide(BigDecimal.valueOf(100));

                savedDatBan.setTienCoc(tienCoc);
                savedDatBan.setTrangThaiCoc(TrangThaiCoc.CHUA_COC.getLabel());

                savedHoaDon.setTienCoc(tienCoc);
                savedHoaDon.setTrangThai(TrangThaiHoaDon.CHO_THANH_TOAN.getCode());
                savedHoaDon.setHinhThucCoc(HinhThucCoc.CHUYEN_KHOAN);

            } else {
                savedDatBan.setTienCoc(null);
                savedDatBan.setTrangThaiCoc(TrangThaiCoc.KHONG_CAN_COC.getLabel());

                savedHoaDon.setTienCoc(null);
                savedHoaDon.setTrangThai(TrangThaiHoaDon.CHO_XAC_NHAN.getCode());
            }
            datBanRepository.save(savedDatBan);
            hoaDonRepository.save(savedHoaDon);
            webSocketService.sendDatBanUpdate(datBan);
            webSocketService.sendHoaDonUpdate(hoaDon);

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                HoaDon finalHoaDon = savedHoaDon;
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            // Gọi hàm sendMailDatBan (hàm chờ xác nhận với template riêng)
                            mailService.sendMailDatBan(savedDatBan, finalHoaDon);
                        } catch (Exception e) {
                            log.error("Lỗi gửi mail đặt bàn online: ", e);
                        }
                    }
                });
            }
            if (tienCoc == null) {
                return null;
            }
            long amount = tienCoc.longValue();
            String vnpayUrl = vNPayService.createPaymentUrl(
                    amount,
                    savedDatBan.getId().toString(),
                    httpRequest
            );
            return vnpayUrl;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi đặt bàn: " + e.getMessage());
        }


    }

    @Override
    public BigDecimal tinhGiaSauGiam(SanPham sp) {
        BigDecimal giaGoc = sp.getGia() == null
                ? BigDecimal.ZERO
                : sp.getGia();

        List<DotGiamGiaSanPham> ds =
                dotGiamGiaSanPhamRepository.findDangApDung(
                        sp.getId(),
                        DateTimeUtils.now()
                );

        if (ds == null || ds.isEmpty()) {
            return giaGoc;
        }

        DotGiamGia dot = ds.get(0).getDotGiamGia();

        BigDecimal phanTramGiam = dot.getGiaTriGiam() == null
                ? BigDecimal.ZERO
                : dot.getGiaTriGiam();

        BigDecimal soTienGiam = giaGoc
                .multiply(phanTramGiam)
                .divide(BigDecimal.valueOf(100));

        return giaGoc.subtract(soTienGiam)
                .max(BigDecimal.ZERO);
    }

}