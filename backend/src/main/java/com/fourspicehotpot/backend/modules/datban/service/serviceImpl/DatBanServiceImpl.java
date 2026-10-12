package com.fourspicehotpot.backend.modules.datban.service.serviceImpl;

import com.fourspicehotpot.backend.common.enums.*;
import com.fourspicehotpot.backend.common.util.DateTimeUtils;
import com.fourspicehotpot.backend.integration.mail.service.MailService;
import com.fourspicehotpot.backend.integration.websocket.service.WebSocketService;
import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.banan.entity.KhuVuc;
import com.fourspicehotpot.backend.modules.banan.repository.BanAnRepository;
import com.fourspicehotpot.backend.modules.datban.dto.request.DatBanRequest;
import com.fourspicehotpot.backend.modules.datban.dto.request.DatBanSearchRequest;
import com.fourspicehotpot.backend.modules.checkin.dto.request.SearchKhachChoCheckInRequest;
import com.fourspicehotpot.backend.modules.datban.dto.request.XepBanRequest;
import com.fourspicehotpot.backend.modules.datban.dto.response.DatBanResponse;
import com.fourspicehotpot.backend.modules.datban.entity.DatBan;
import com.fourspicehotpot.backend.modules.datban.mapper.DatBanMapper;
import com.fourspicehotpot.backend.modules.datban.repository.DatBanRepository;
import com.fourspicehotpot.backend.modules.datban.service.DatBanService;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDonChiTiet;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonChiTietRepository;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonRepository;
import com.fourspicehotpot.backend.modules.hoadon.service.LichSuHoaDonService;
import com.fourspicehotpot.backend.modules.hoadon.service.LichSuThanhToanService;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.khachhang.repository.KhachHangRepository;
import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import com.fourspicehotpot.backend.modules.nhanvien.repository.NhanVienRepository;
import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import com.fourspicehotpot.backend.modules.setlau.repository.SanPhamRepository;
import com.fourspicehotpot.backend.modules.setlau.service.SanPhamService;
import com.fourspicehotpot.backend.modules.thamsohethong.service.ThamSoHeThongService;
import com.fourspicehotpot.backend.modules.thucdon.dto.request.CustomerMonDatTruocRequest;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DatBanServiceImpl implements DatBanService {

    private static final Logger log = LoggerFactory.getLogger(DatBanServiceImpl.class);

    private final DatBanRepository datBanRepository;
    private final KhachHangRepository khachHangRepository;
    private final BanAnRepository banAnRepository;
    private final HoaDonRepository hoaDonRepository;
    private final ThamSoHeThongService thamSoHeThongService;
    private final SanPhamRepository sanPhamRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final MailService mailService;
    private final LichSuHoaDonService lichSuHoaDonService;
    private final LichSuThanhToanService lichSuThanhToanService;
    private final NhanVienRepository nhanVienRepository;
    private final WebSocketService webSocketService;
    private final DatBanMapper datBanMapper;
    private final SanPhamService sanPhamService;


    // Không được sửa - Lãnh Minh Hiếu
    @Override
    public List<DatBanResponse> findAll() {
        List<DatBan> list = datBanRepository.findAllWithBan();
        System.out.println("Số bản ghi đặt bàn: " + list.size());

        return list.stream()
                .map(datBan -> {
                    try {
                        return datBanMapper.mapToResponseHienThi(datBan);
                    } catch (Exception e) {
                        System.out.println("Lỗi tại DatBan ID: " + datBan.getId());
                        e.printStackTrace();
                        throw e;
                    }
                })
                .toList();
    }

    @Override
    public DatBanResponse findById(Long id) {
        DatBan datBan = datBanRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));
        return datBanMapper.mapToResponse(datBan);
    }

    // Không được sửa - Lãnh Minh Hiếu -- Tạm thời không dùng
    @Override
    public DatBanResponse xacNhanDatBan(Long id) {
        DatBan db = datBanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy"));

        if (db.getTrangThai().equals(TrangThaiDatBan.DA_HUY.getCode())) {
            throw new RuntimeException("Đơn đã hủy, không thể xác nhận");
        }

        if (db.getTrangThai().equals(TrangThaiDatBan.DA_XAC_NHAN.getCode())) {
            throw new RuntimeException("Đơn đã được xác nhận trước đó");
        }

        db.setTrangThai(TrangThaiDatBan.DA_XAC_NHAN.getCode());
        datBanRepository.save(db);
        HoaDon hoaDon = hoaDonRepository
                .findByDatBanId(db.getId())
                .orElse(null);

        if (hoaDon != null) {
            hoaDon.setTrangThai(TrangThaiHoaDon.DA_XAC_NHAN.getCode());
            hoaDonRepository.save(hoaDon);
        }
        if (hoaDon != null) {
            lichSuHoaDonService.saveLichSuHoaDon(
                    hoaDon,
                    "XAC_NHAN_DAT_BAN",
                    0,
                    "Xác nhận đặt bàn cho khách hàng"
            );
        }

        try {
            Long idDatBan = Long.valueOf(db.getId());
            Long idHoaDon = hoaDon.getId();
            mailService.sendMailXacNhan(idDatBan, idHoaDon);

            if (hoaDon != null) {
                lichSuHoaDonService.saveLichSuHoaDon(
                        hoaDon,
                        "GUI_MAIL",
                        0,
                        "Gửi mail xác nhận cho khách hàng"
                );
            }

        } catch (Exception e) {
            System.out.println("Gửi mail lỗi nhưng không ảnh hưởng hệ thống");
        }

        return datBanMapper.mapToResponse(db);
    }

    // Không được sửa - Lãnh Minh Hiếu
    @Override
    public DatBanResponse xacNhanCocVaXacNhan(DatBanRequest request) {
        boolean coCoc = request.getTienCoc() != null && request.getTienCoc().compareTo(BigDecimal.ZERO) > 0;

        DatBan db = datBanRepository.findById(Long.valueOf(request.getId()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));
        if (db.getTrangThai().equals(TrangThaiDatBan.DA_HUY.getCode())) {
            throw new RuntimeException("Đơn đã hủy, không thể xác nhận");
        }

        if (db.getTrangThai().equals(TrangThaiDatBan.DA_XAC_NHAN.getCode())) {
            throw new RuntimeException("Đơn đã được xác nhận trước đó");
        }
        if (coCoc) {
            db.setTienCoc(request.getTienCoc());
            db.setTrangThaiCoc(TrangThaiCoc.DA_COC.name());
            db.setPhanTramCoc(BigDecimal.valueOf(thamSoHeThongService.getDouble("PHAN_TRAM_COC")));
        } else {
            db.setTrangThaiCoc(TrangThaiCoc.KHONG_CAN_COC.name());
            db.setTienCoc(null);
        }
        db.setTrangThai(TrangThaiDatBan.DA_XAC_NHAN.getCode());
        datBanRepository.save(db);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String taiKhoan = auth.getName();
        NhanVien nhanVien = nhanVienRepository.findNhanVienByTaiKhoan(taiKhoan)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // Lấy hóa đơn liên quan
        HoaDon hoaDon = hoaDonRepository.findByDatBanId(db.getId()).orElse(null);
        if (hoaDon != null) {
            hoaDon.setTienCoc(request.getTienCoc());
            hoaDon.setTrangThai(TrangThaiHoaDon.DA_XAC_NHAN.getCode());
            hoaDon.setHinhThucCoc(request.getHinhThucCoc());
            hoaDon.setNhanVien(nhanVien);
            hoaDon.setNguoiTao(nhanVien.getId());
            hoaDonRepository.save(hoaDon);
        }
        if (hoaDon != null && coCoc) {

            lichSuThanhToanService.save(
                    hoaDon,
                    String.valueOf(request.getHinhThucCoc()),
                    request.getTienCoc(),
                    "Thanh toán tiền cọc đặt bàn",
                    LoaiThanhToan.TIEN_COC
            );
        }
        if (hoaDon != null) {
            lichSuHoaDonService.saveLichSuHoaDon(
                    hoaDon,
                    "NHAN_TIEN_COC",
                    0,
                    "Xác nhận tiền cọc từ khách hàng"
            );
        }
        if (hoaDon != null) {
            lichSuHoaDonService.saveLichSuHoaDon(
                    hoaDon,
                    "XAC_NHAN_DAT_BAN",
                    0,
                    "Xác nhận đặt bàn cho khách hàng"
            );
        }
        try {
            Long idDatBan = Long.valueOf(db.getId());
            Long idHoaDon = hoaDon.getId();
            mailService.sendMailXacNhan(idDatBan, idHoaDon);

            if (hoaDon != null) {
                lichSuHoaDonService.saveLichSuHoaDon(
                        hoaDon,
                        "GUI_MAIL",
                        0,
                        "Gửi mail xác nhận cho khách hàng"
                );
            }

        } catch (Exception e) {
            System.out.println("Gửi mail lỗi nhưng không ảnh hưởng hệ thống");
        }

        return datBanMapper.mapToResponse(db);
    }

    // Không được sửa - Lãnh Minh Hiếu
    @Transactional(rollbackFor = Exception.class)
    @Override
    public DatBanResponse datBanTaiQuay(DatBanRequest request) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        int min = thamSoHeThongService.getInt("DAT_TRUOC_TOI_THIEU");
        int max = thamSoHeThongService.getInt("DAT_TRUOC_TOI_DA_NGAY");

        int thoiGianSuDung = thamSoHeThongService.getInt("THOI_GIAN_SU_DUNG_BAN_MAC_DINH");
        LocalDateTime thoiGianKetThucDuKien = request.getThoiGianDat().plusMinutes(thoiGianSuDung);

        // Có món thì cọc không thì thôi
        boolean coMon = request.getMonDatTruoc() != null
                && !request.getMonDatTruoc().isEmpty();

        if (request.getThoiGianDat().isBefore(now.plusMinutes(min))) {
            throw new RuntimeException("Đặt trước tối thiểu " + min + " phút");
        }
        if (request.getThoiGianDat().isAfter(now.plusDays(max))) {
            throw new RuntimeException("Đặt trước tối đa " + max + " ngày");
        }

        // 1. Tìm hoặc tạo khách hàng
        Optional<KhachHang> optionalKhachHang =
                khachHangRepository.findBySoDienThoai(request.getSoDienThoai());
        KhachHang khachHang;

        if (optionalKhachHang.isEmpty()) {
            khachHang = new KhachHang();
            khachHang.setSoDienThoai(request.getSoDienThoai());
            khachHang.setTenKhachHang(request.getTenKhachHang());
            khachHang.setEmail(request.getEmail());
            khachHang.setNgaySinh(request.getNgaySinh());
            khachHang.setMatKhau("khachvanglai");
            khachHang = khachHangRepository.save(khachHang);
        } else {
            khachHang = optionalKhachHang.get();
        }

        int thoiGianCoc = thamSoHeThongService.getInt("THOI_GIAN_CHO_THANH_TOAN_COC");
        LocalDateTime hanThanhToanCoc = now.plusMinutes(thoiGianCoc);

        // Đặt nhiều bàn hoặc không
        List<BanAn> danhSachBan = new ArrayList<>();
        if (request.getListIdBan() != null && !request.getListIdBan().isEmpty()) {
            danhSachBan = banAnRepository.findAllByIdForUpdate(request.getListIdBan());
            if (danhSachBan.size() != request.getListIdBan().size()) {
                throw new RuntimeException("Có bàn không tồn tại");
            }
            for (BanAn ban : danhSachBan) {
                boolean trungLich = datBanRepository.existsOverlap(
                        ban.getId(),
                        request.getThoiGianDat(),
                        thoiGianKetThucDuKien
                );
                if (trungLich) {
                    throw new RuntimeException(
                            "Bàn " + ban.getTenBanAn() + " đã có lịch đặt"
                    );
                }
            }
            for (BanAn ban : danhSachBan) {
                ban.setTrangThai(TrangThaiBan.DA_DAT.getValue());
            }
            banAnRepository.saveAll(danhSachBan);
        }
        DatBan entity = DatBan.builder()
                .banAns(danhSachBan)
                .khachHang(khachHang)
                .thoiGianDat(request.getThoiGianDat())
                .thoiGianKetThucDuKien(thoiGianKetThucDuKien)
                .soNguoiToiDa(request.getSoNguoiToiDa())
                .ghiChu(request.getGhiChu())
                .hinhThucDat(2)
                .trangThai(coMon ? TrangThaiDatBan.CHO_XAC_NHAN.getCode() : TrangThaiDatBan.DA_XAC_NHAN.getCode())
                .tienCoc(null)
                .phanTramCoc(null)
                .hanThanhToanCoc(coMon ? hanThanhToanCoc : null)
                .trangThaiCoc(
                        coMon
                                ? TrangThaiCoc.CHUA_COC.name()
                                : TrangThaiCoc.KHONG_CAN_COC.name()
                )
                .build();
        DatBan savedDatBan = datBanRepository.save(entity);

        // 3. LUÔN tạo hóa đơn (rỗng hoặc có món)
        HoaDon hoaDon = new HoaDon();
        String ma = "HD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 7).toUpperCase();
        hoaDon.setMaHoaDon(ma);
        hoaDon.setDatBan(savedDatBan);
        hoaDon.setKhachHang(khachHang);
        hoaDon.setBanAns(danhSachBan);
        hoaDon.setTrangThai(coMon ? TrangThaiHoaDon.CHO_COC.getCode() : TrangThaiHoaDon.DA_XAC_NHAN.getCode());
        hoaDon.setLoaiHoaDon(1);
        hoaDon.setTienCoc(savedDatBan.getTienCoc());
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
        lichSuHoaDonService.saveLichSuHoaDon(
                savedHoaDon,
                "TAO_HOA_DON",
                0,
                "Tạo hóa đơn tại quầy"
        );


        String tenBan = entity.getBanAns().stream()
                .map(BanAn::getTenBanAn)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        String ghiChu = "Xếp bàn [" + tenBan + "] cho khách "
                + entity.getKhachHang().getTenKhachHang();
        lichSuHoaDonService.saveLichSuHoaDon(
                savedHoaDon,
                "XEP_BAN",
                0,
                ghiChu
        );

        List<Map<String, Object>> monEmailList = new ArrayList<>();
        BigDecimal tongTienMon = BigDecimal.ZERO;

        if (request.getMonDatTruoc() != null && !request.getMonDatTruoc().isEmpty()) {

            for (CustomerMonDatTruocRequest mon : request.getMonDatTruoc()) {

                SanPham sanPham =
                        sanPhamRepository.findById(mon.getIdSanPham())
                                .orElseThrow(() ->
                                        new RuntimeException("KHONG TIM THAY SAN PHAM: " + mon.getIdSanPham()));

                HoaDonChiTiet chiTiet = new HoaDonChiTiet();

                BigDecimal giaSauGiam = sanPhamService.tinhGiaSauGiam(sanPham);
                chiTiet.setHoaDon(savedHoaDon);
                chiTiet.setSanPham(sanPham);
                chiTiet.setSoLuong(mon.getSoLuong());
                chiTiet.setDonGia(giaSauGiam);
                chiTiet.setTrangThai(0);
                chiTiet.setNgayTao(now);
                BigDecimal thanhTien = giaSauGiam
                        .multiply(BigDecimal.valueOf(mon.getSoLuong()));

                chiTiet.setThanhTien(thanhTien);
                tongTienMon = tongTienMon.add(thanhTien);

                hoaDonChiTietRepository.save(chiTiet);
                Map<String, Object> monMap = new HashMap<>();
                monMap.put("tenSanPham", sanPham.getTenSanPham());
                monMap.put("soLuong", mon.getSoLuong());
                monMap.put("gia", sanPham.getGia());

                monEmailList.add(monMap);
            }
            savedHoaDon.setTongTienMon(tongTienMon);
            savedHoaDon.setTongTien(tongTienMon);
            hoaDonRepository.save(savedHoaDon);

        }
        if (!coMon) {
            lichSuHoaDonService.saveLichSuHoaDon(
                    savedHoaDon,
                    "XAC_NHAN",
                    0,
                    "Xác nhận đặt bàn tại quầy"
            );
            try {
                mailService.sendMailXacNhan(Long.valueOf(savedDatBan.getId()), savedHoaDon.getId());
            } catch (Exception e) {
                System.err.println("Lỗi gọi mail service: " + e.getMessage());
            }
        }
        webSocketService.sendRealtimeChonBan();
        return datBanMapper.mapToResponse(savedDatBan);
    }


    @Override
    public List<DatBanResponse> findForCheckIn() {
        // Lấy ngày hôm nay
        LocalDateTime today = LocalDateTime.now();

        // Gọi repository để lấy danh sách đặt trong ngày hôm nay
        List<DatBan> list = datBanRepository.findByDate(today);

        return list.stream()
                .map(datBanMapper::mapToResponse)
                .toList();
    }


    // Không được sửa
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DatBanResponse xepBanTaiQuay(XepBanRequest request) {

        DatBan datBan = datBanRepository.findById(request.getIdDatBan())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));


        if (datBan.getBanAns() != null && !datBan.getBanAns().isEmpty()) {
            throw new RuntimeException("Đặt bàn đã được xếp bàn!");
        }

        int thoiGianSuDung = thamSoHeThongService.getInt("THOI_GIAN_SU_DUNG_BAN_MAC_DINH");
        LocalDateTime thoiGianKetThucDuKien = datBan.getThoiGianDat().plusMinutes(thoiGianSuDung);

        if (request.getListIdBan() == null
                || request.getListIdBan().isEmpty()) {
            throw new RuntimeException("Vui lòng chọn bàn");
        }

        List<BanAn> danhSachBan = banAnRepository
                .findAllById(request.getListIdBan());

        if (danhSachBan.size() != request.getListIdBan().size()) {
            throw new RuntimeException("Có bàn không tồn tại");
        }
        thamSoHeThongService.validateSucChuaBan(datBan, danhSachBan);

        for (BanAn ban : danhSachBan) {
            boolean trungLich = datBanRepository.existsOverlap(
                    ban.getId(),
                    datBan.getThoiGianDat(),
                    thoiGianKetThucDuKien

            );
            if (trungLich) {
                throw new RuntimeException(
                        "Bàn " + ban.getTenBanAn() + " đã có lịch đặt"
                );
            }
        }

        for (BanAn ban : danhSachBan) {
            ban.setTrangThai(TrangThaiBan.DA_DAT.getValue());
        }
        banAnRepository.saveAll(danhSachBan);

        HoaDon hoaDon = hoaDonRepository.findByDatBan(datBan)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        datBan.setThoiGianKetThucDuKien(thoiGianKetThucDuKien);
        datBan.setBanAns(danhSachBan);
        hoaDon.setBanAns(danhSachBan);
        hoaDon.setTrangThai(TrangThaiHoaDon.DA_XAC_NHAN.getCode());
        datBan.setTrangThai(TrangThaiDatBan.DA_XAC_NHAN.getCode());

        datBanRepository.save(datBan);
        hoaDonRepository.save(hoaDon);

        String tenBan = datBan.getBanAns().stream()
                .map(BanAn::getTenBanAn)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String ghiChu = "Xếp bàn [" + tenBan + "] cho khách "
                + datBan.getKhachHang().getTenKhachHang();
        lichSuHoaDonService.saveLichSuHoaDon(
                hoaDon,
                "XEP_BAN",
                0,
                ghiChu
        );

        Long idDatBan = Long.valueOf(datBan.getId());
        Long idHoaDon = hoaDon.getId();
        try {
            mailService.sendMailXacNhan(idDatBan, idHoaDon);
        } catch (Exception e) {
            System.err.println("Lỗi gọi mail service: " + e.getMessage());
        }
        return datBanMapper.mapToResponse(datBan);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DatBanResponse xepLaiBan(XepBanRequest request) {
        DatBan datBan = datBanRepository.findById(request.getIdDatBan()).
                orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));

        int thoiGianSuDung = thamSoHeThongService.getInt("THOI_GIAN_SU_DUNG_BAN_MAC_DINH");
        LocalDateTime thoiGianKetThucDuKien = datBan.getThoiGianDat().plusMinutes(thoiGianSuDung);

        if (request.getListIdBan() == null || request.getListIdBan().isEmpty()) {
            throw new RuntimeException("Vui lòng chọn bàn");
        }

        List<BanAn> lstBanAn = banAnRepository.findAllById(request.getListIdBan());
        if (lstBanAn.size() != request.getListIdBan().size()) {
            throw new RuntimeException("Có bàn ăn không tồn tại");
        }
        thamSoHeThongService.validateSucChuaBan(datBan, lstBanAn);

        for (BanAn ban : lstBanAn) {
            boolean trungLich = datBanRepository.existsOverlapChinhMinh(
                    ban.getId(),
                    datBan.getId(),
                    datBan.getThoiGianDat(),
                    thoiGianKetThucDuKien);
            if (trungLich) {
                throw new RuntimeException("Bàn " + ban.getTenBanAn() + " đã có lịch đặt");
            }
        }


        HoaDon hoaDon = hoaDonRepository.findByDatBan(datBan).orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        List<BanAn> lstBanCu = new ArrayList<>(datBan.getBanAns());
        for (BanAn ban : lstBanAn) {
            ban.setTrangThai(TrangThaiBan.DA_DAT.getValue());
        }
        banAnRepository.saveAll(lstBanAn);

        datBan.setBanAns(lstBanAn);
        hoaDon.setBanAns(lstBanAn);


        datBanRepository.save(datBan);
        hoaDonRepository.save(hoaDon);

        for (BanAn ban : lstBanCu) {
            if (!dangDuocSuDung(ban.getId())) {

                ban.setTrangThai(TrangThaiBan.TRONG.getValue());
            }
        }
        banAnRepository.saveAll(lstBanCu);


        lichSuHoaDonService.saveLichSuHoaDon(
                hoaDon,
                "XEP_LAI_BAN",
                0,
                "Xếp lại bàn cho khách " + datBan.getKhachHang().getTenKhachHang()
        );

        return datBanMapper.mapToResponse(datBan);
    }

    @Override
    public boolean dangDuocSuDung(Integer idBan) {
        boolean coHoaDon = hoaDonRepository.existsHoaDonDangSuDungBan(idBan, List.of(
                TrangThaiHoaDon.KHACH_DEN.getCode(),
                TrangThaiHoaDon.DANG_PHUC_VU.getCode(),
                TrangThaiHoaDon.CHO_THANH_TOAN.getCode()
        ));
        if (coHoaDon) {
            return true;
        }
        int thoiGianSuDung = thamSoHeThongService.getInt("THOI_GIAN_SU_DUNG_BAN_MAC_DINH");
        return datBanRepository.existsDatBanDangSuDungBan(idBan, DateTimeUtils.now(), thoiGianSuDung, TrangThaiDatBan.DA_HUY.getCode());
    }

    @Override
    public List<DatBanResponse> searchDatBan(DatBanSearchRequest request) {
        Specification<DatBan> spec = (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // SĐT
            Join<DatBan, KhachHang> khachHangJoin = null;

            if (request.getSoDienThoai() != null && !request.getSoDienThoai().isBlank()) {
                khachHangJoin = root.join("khachHang", JoinType.LEFT);

                predicates.add(
                        cb.like(
                                cb.lower(khachHangJoin.get("soDienThoai")),
                                "%" + request.getSoDienThoai().toLowerCase() + "%"
                        )
                );
            }
            // Trạng thái
            if (request.getTrangThai() != null && !request.getTrangThai().isBlank()) {
                predicates.add(
                        cb.equal(root.get("trangThai"), request.getTrangThai())
                );
            }

            // Trạng thái cọc
            if (request.getTrangThaiCoc() != null && !request.getTrangThaiCoc().isBlank()) {
                predicates.add(
                        cb.equal(root.get("trangThaiCoc"), request.getTrangThaiCoc())
                );
            }
            // Thời gian
            if (request.getThoiGianDat() != null) {
                LocalDate date = request.getThoiGianDat();
                predicates.add(
                        cb.between(
                                root.get("thoiGianDat"),
                                date.atStartOfDay(),
                                date.atTime(23, 59, 59)
                        )
                );
            }
            // Tầng
            if (request.getTenKhuVuc() != null && !request.getTenKhuVuc().isBlank()) {

                Join<DatBan, BanAn> banAnJoin = root.join("banAns", JoinType.LEFT);
                Join<BanAn, KhuVuc> khuVucJoin = banAnJoin.join("khuVuc", JoinType.LEFT);

                predicates.add(
                        cb.like(
                                cb.lower(khuVucJoin.get("tenKhuVuc")),
                                "%" + request.getTenKhuVuc().toLowerCase() + "%"
                        )
                );
            }
            query.distinct(true);
            return cb.or(predicates.toArray(new Predicate[0]));
        };
        return datBanRepository.findAll(spec)
                .stream()
                .map(datBanMapper::mapToResponseHienThi)
                .toList();
    }

    // Không được sửa -> Long sửa cắt tay
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoHuyDatBan() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        List<DatBan> list = datBanRepository
                .findDatBanCanHuy(
                        TrangThaiDatBan.CHUA_COC.getCode(),
                        now
                );
        for (DatBan datBan : list) {

            if (datBan.getTrangThai().equals(TrangThaiDatBan.DA_HUY.getCode())) {
                continue;
            }

            if (datBan.getBanAns() != null) {
                for (BanAn banAn : datBan.getBanAns()) {
                    banAn.setTrangThai(TrangThaiBan.TRONG.getValue());
                }

            }
            datBan.setTrangThai(TrangThaiDatBan.DA_HUY.getCode());
            datBan.setTrangThaiCoc(TrangThaiDatBan.HUY_COC.getCode());
            datBan.setNgayCapNhat(LocalDateTime.now());
            datBanRepository.save(datBan);
            Optional<HoaDon> optionalHoaDon = hoaDonRepository.findByDatBanId(datBan.getId());

            if (optionalHoaDon.isPresent()) {
                HoaDon hoaDon = optionalHoaDon.get();

                hoaDon.setTrangThai(TrangThaiHoaDon.DA_HUY.getCode());
                hoaDon.setNgayCapNhat(LocalDateTime.now());
                hoaDonRepository.save(hoaDon);
                lichSuHoaDonService.saveLichSuHoaDon(
                        hoaDon,
                        "AUTO_HUY",
                        0,
                        "Hệ thống tự động hủy do quá hạn thanh toán cọc"
                );


                Long idDatBan = Long.valueOf(datBan.getId());
                Long idHoaDon = hoaDon.getId();
                try {
                    mailService.sendMailHuyDoQuaHanCoc(idDatBan, idHoaDon);
                } catch (Exception e) {
                    System.err.println("Lỗi gọi mail service: " + e.getMessage());
                }

                webSocketService.sendHoaDonUpdate(hoaDon);
            }
            webSocketService.sendDatBanUpdate(datBan);
            webSocketService.sendDatBanDetailUpdate(Long.valueOf(datBan.getId()));


        }

    }
}