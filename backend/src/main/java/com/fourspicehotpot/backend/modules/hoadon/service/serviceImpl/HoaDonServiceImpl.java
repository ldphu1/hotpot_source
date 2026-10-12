package com.fourspicehotpot.backend.modules.hoadon.service.serviceImpl;

import com.fourspicehotpot.backend.common.enums.*;
import com.fourspicehotpot.backend.common.util.DateTimeUtils;
import com.fourspicehotpot.backend.integration.mail.utils.SendMail;
import com.fourspicehotpot.backend.integration.pdf.utils.HoaDonPdf;
import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.banan.entity.KhuVuc;
import com.fourspicehotpot.backend.modules.banan.repository.BanAnRepository;
import com.fourspicehotpot.backend.modules.checkin.dto.request.CheckInRequest;
import com.fourspicehotpot.backend.modules.datban.entity.DatBan;
import com.fourspicehotpot.backend.modules.datban.repository.DatBanRepository;
import com.fourspicehotpot.backend.modules.hoadon.dto.request.HoaDonRequest;
import com.fourspicehotpot.backend.modules.hoadon.dto.request.HoaDonSearchRequest;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonChiTietResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.TienCocPreviewResponse;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDonBanAn;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDonChiTiet;
import com.fourspicehotpot.backend.modules.hoadon.entity.LichSuHoaDon;
import com.fourspicehotpot.backend.modules.hoadon.mapper.HoaDonMapper;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonBanAnRepository;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonChiTietRepository;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonRepository;
import com.fourspicehotpot.backend.modules.hoadon.repository.LichSuHoaDonRepository;
import com.fourspicehotpot.backend.modules.hoadon.service.HoaDonService;
import com.fourspicehotpot.backend.modules.hoadon.service.LichSuHoaDonService;
import com.fourspicehotpot.backend.modules.hoadon.service.LichSuThanhToanService;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.thamsohethong.service.ThamSoHeThongService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class HoaDonServiceImpl implements HoaDonService {
    @Autowired
    HoaDonRepository hoaDonRepository;
    @Autowired
    BanAnRepository banAnRepository;
    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;
    @Autowired
    private DatBanRepository datBanRepository;
    @Autowired
    private ThamSoHeThongService thamSoHeThongService;
    @Autowired
    private LichSuHoaDonService lichSuHoaDonService;
    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Autowired
    private HoaDonPdf pdfUtil;
    @Autowired
    private HoaDonMapper hoaDonMapper;
    @Autowired
    private LichSuThanhToanService lichSuThanhToanService;
    @Autowired
    private HoaDonBanAnRepository hoaDonBanAnRepository;

    @Override
    public HoaDon checkin(CheckInRequest request) {

        BanAn ban = banAnRepository.findById(Math.toIntExact(request.getIdBanAn()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn"));

        HoaDon hoaDon = HoaDon.builder()
                .banAns(List.of(ban))
                .loaiHoaDon(1)
                .thoiGianMo(LocalDateTime.now())
                .trangThai(0)
                .build();

        ban.setTrangThai(3); // bàn đang dùng

        banAnRepository.save(ban);

        return hoaDonRepository.save(hoaDon);
    }

    @Override
    public HoaDon getHoaDonDangMoByBan(Long idBan) {

        return hoaDonRepository
                .findByBanAn_IdAndTrangThai(idBan, 0)
                .orElseThrow(() -> new RuntimeException("Bàn chưa có hóa đơn"));
    }

    @Override
    public HoaDon thanhToan(Long idHoaDon) {

        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        hoaDon.setTrangThai(1); // đã thanh toán
        hoaDon.setThoiGianDong(LocalDateTime.now());
        hoaDon.setThoiGianThanhToan(LocalDateTime.now());
        if (hoaDon.getBanAns() != null) {
            for (BanAn ban : hoaDon.getBanAns()) {
                ban.setTrangThai(4); // đang dọn
                banAnRepository.save(ban);
            }
        }


        return hoaDonRepository.save(hoaDon);
    }

    // Không được sửa - Lãnh Minh Hiếu
    @Override
    public List<HoaDonResponse> hienThi() {
        List<HoaDon> list = hoaDonRepository.findAllHoaDon();
        return list.stream()
                .map(hoaDonMapper::mapToResponse)
                .toList();
    }


    // Không được sửa - Lãnh Minh Hiếu
    @Override
    public void chuyenTrangThai(Long idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (!hoaDon.getTrangThai().equals(TrangThaiHoaDon.VUA_TAO.getCode())) {
            throw new RuntimeException("Chỉ được xác nhận hóa đơn ở trạng thái Vừa tạo");
        }
        hoaDon.setTrangThai(1);

        DatBan datBan = hoaDon.getDatBan();

        if (datBan != null) {
            datBan.setTrangThai("DA_XAC_NHAN");
            datBanRepository.save(datBan);
        }

        LichSuHoaDon log = new LichSuHoaDon();
        log.setHoaDon(hoaDon);
        log.setHanhDong("Xác nhận hóa đơn");
        log.setThoiGian(LocalDateTime.now());
        log.setGhiChu("Đơn hàng đã được xác nhận");
        lichSuHoaDonRepository.save(log);

        hoaDonRepository.save(hoaDon);

        KhachHang khach = hoaDon.getKhachHang();
        hoaDon.getHoaDonChiTiets().size();

        if (khach != null && khach.getEmail() != null) {

            StringBuilder danhSachMon = new StringBuilder();

            for (HoaDonChiTiet ct : hoaDon.getHoaDonChiTiets()) {
                String tenMon = ct.getSanPham().getTenSanPham();
                int soLuong = ct.getSoLuong();

                danhSachMon.append("""
                            <tr>
                                <td style="padding:8px;border-bottom:1px solid #ddd;">%s</td>
                                <td style="padding:8px;border-bottom:1px solid #ddd;text-align:center;">x%d</td>
                            </tr>
                        """.formatted(tenMon, soLuong));
            }
            String tenBan = "Chưa xếp bàn";

            if (hoaDon.getBanAns() != null && !hoaDon.getBanAns().isEmpty()) {
                tenBan = hoaDon.getBanAns()
                        .stream()
                        .map(BanAn::getTenBanAn)
                        .collect(Collectors.joining(", "));
            }

            String html = """
                        <html>
                        <body style="font-family: Arial; background:#f6f6f6; padding:20px;">
                    
                            <div style="max-width:600px;margin:auto;background:white;padding:20px;border-radius:10px;">
                    
                                <h2 style="text-align:center;color:#e67e22;">
                                    🎉 ĐẶT BÀN THÀNH CÔNG
                                </h2>
                    
                                <p>Xin chào <b>%s</b>,</p>
                    
                                <p>Phiếu đặt bàn của bạn đã được xác nhận.</p>
                    
                                <p><b>🕒 Thời gian:</b> %s</p>
                                <p><b>👥 Số khách:</b> %s</p>
                                <p><b>🍽 Bàn:</b> %s</p>
                    
                                <h3 style="margin-top:20px;">🍲 Món đã đặt:</h3>
                    
                                <table style="width:100%%;border-collapse:collapse;">
                                    %s
                                </table>
                    
                                <img src="cid:bannerImage" 
                                     style="width:100%%;margin-top:20px;border-radius:8px"/>
                    
                                <p style="text-align:center;margin-top:20px;">
                                    ❤️ Cảm ơn bạn đã chọn FourSpiceHotpot
                                </p>
                            </div>
                    
                        </body>
                        </html>
                    """.formatted(
                    khach.getTenKhachHang(), // sửa đúng field tên bên ông
                    datBan != null ? datBan.getThoiGianDat() : "Không rõ",
                    datBan != null ? datBan.getSoNguoiToiDa() : "Không rõ",
                    tenBan
                    ,
                    danhSachMon.toString()
            );
            try {
                SendMail.sendMail(khach.getEmail(), "Xác nhận đặt bàn", html);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public List<HoaDonResponse> search(HoaDonSearchRequest request) {
        Specification<HoaDon> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<HoaDon, KhachHang> hoaDonKhachHangJoin = null;
            if (request.getSoDienThoai() != null && !request.getSoDienThoai().isEmpty()) {
                hoaDonKhachHangJoin = root.join("khachHang", JoinType.LEFT);
                predicates.add(
                        cb.like(
                                cb.lower(hoaDonKhachHangJoin.get("soDienThoai")),
                                "%" + request.getSoDienThoai() + "%"
                        )
                );
            }

            if (request.getTenKhachHang() != null && !request.getTenKhachHang().isEmpty()) {
                hoaDonKhachHangJoin = root.join("khachHang", JoinType.LEFT);
                predicates.add(
                        cb.like(
                                cb.lower(hoaDonKhachHangJoin.get("tenKhachHang")),
                                "%" + request.getTenKhachHang() + "%"
                        )
                );
            }
            if (request.getTrangThai() != null && !request.getTrangThai().isEmpty()) {
                predicates.add(
                        cb.equal(root.get("trangThai"), request.getTrangThai())
                );
            }
            if (request.getLoaiHoaDon() != null && !request.getLoaiHoaDon().isEmpty()) {
                predicates.add(
                        cb.equal(root.get("loaiHoaDon"), request.getLoaiHoaDon())
                );
            }
            if (request.getTenKhuVuc() != null && !request.getTenKhuVuc().isEmpty()) {
                Join<HoaDon, DatBan> hoaDonDatBanJoin = root.join("datBan", JoinType.LEFT);
                Join<DatBan, BanAn> datBanBanAnJoin = hoaDonDatBanJoin.join("banAns", JoinType.LEFT);
                Join<BanAn, KhuVuc> banAnKhuVucJoin = datBanBanAnJoin.join("khuVuc", JoinType.LEFT);

                predicates.add(
                        cb.like(
                                cb.lower(banAnKhuVucJoin.get("tenKhuVuc")),
                                "%" + request.getTenKhuVuc() + "%"
                        )
                );
            }
            if (request.getNgayBatDau() != null && request.getNgayKetThuc() != null) {
                predicates.add(
                        cb.between(
                                root.get("thoiGianThanhToan"),
                                request.getNgayBatDau().atStartOfDay(),
                                request.getNgayKetThuc().atTime(23, 59, 59)
                        )
                );
            } else if (request.getNgayBatDau() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("thoiGianThanhToan"),
                                request.getNgayBatDau().atStartOfDay()
                        )
                );
            } else if (request.getNgayKetThuc() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("thoiGianThanhToan"),
                                request.getNgayKetThuc().atTime(23, 59, 59)
                        )
                );
            }
            query.distinct(true);
            if (predicates.isEmpty()) {
                cb.conjunction();
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
        return hoaDonRepository.findAll(spec).stream().map(hoaDonMapper::mapToResponse).toList();
    }

    @Override
    public void huyHoaDon(Long id, HoaDonRequest request) {
        int thoiGianHoanCoc = thamSoHeThongService.getInt(ThamSoHeThong.THOI_GIAN_HUY_HOAN_COC.getCode());

        HoaDon entity = hoaDonRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (entity.getTrangThai().equals(TrangThaiHoaDon.DA_THANH_TOAN.getCode())
                || entity.getTrangThai().equals(TrangThaiHoaDon.HOAN_THANH.getCode())) {
            throw new RuntimeException("Hóa đơn đã thanh toán không thể hủy");
        }
        BigDecimal tienCoc = entity.getTienCoc() == null ? BigDecimal.ZERO : entity.getTienCoc();
        BigDecimal tienCocHoan = BigDecimal.ZERO;
        BigDecimal tienCocBiMat = BigDecimal.ZERO;

        if (tienCoc.compareTo(BigDecimal.ZERO) > 0 && entity.getDatBan() != null) {
            LocalDateTime thoiGianDat = entity.getDatBan().getThoiGianDat();


            long minutes = ChronoUnit.MINUTES.between(DateTimeUtils.now(), thoiGianDat);

            if (minutes < 0) {
                tienCocHoan = BigDecimal.ZERO;
            } else if (minutes >= thoiGianHoanCoc) {
                tienCocHoan = tienCoc;
            } else {
                tienCocHoan = BigDecimal.ZERO;
            }

            tienCocBiMat = tienCoc.subtract(tienCocHoan);
        }
        entity.setTrangThai(TrangThaiHoaDon.DA_HUY.getCode());
        entity.setGhiChu(request.getGhiChu());
        entity.setNgayCapNhat(DateTimeUtils.now());
        entity.setTienCocHoan(tienCocHoan);
        entity.setTienCocBiMat(tienCocBiMat);

        if (entity.getDatBan() != null) {
            DatBan datBan = datBanRepository.findDatBanById(
                    entity.getDatBan().getId()).orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));
            datBan.setTrangThai(TrangThaiDatBan.DA_HUY.getCode());
            datBan.setGhiChu(request.getGhiChu());

            List<Integer> trangThaiDangHoatDong = List.of(
                    TrangThaiHoaDon.VUA_TAO.getCode(),
                    TrangThaiHoaDon.CHO_COC.getCode(),
                    TrangThaiHoaDon.DA_COC.getCode(),
                    TrangThaiHoaDon.DA_XAC_NHAN.getCode(),
                    TrangThaiHoaDon.KHACH_DEN.getCode(),
                    TrangThaiHoaDon.DANG_PHUC_VU.getCode(),
                    TrangThaiHoaDon.CHO_THANH_TOAN.getCode()
            );

            List<HoaDonBanAn> dsLienKet = hoaDonBanAnRepository.findByHoaDon_Id(id);
            for (HoaDonBanAn lk : dsLienKet) {
                BanAn ban = lk.getBanAn();
                List<HoaDonBanAn> activeBills = hoaDonBanAnRepository.findHoaDonDangHoatDongByBan(ban.getId(), trangThaiDangHoatDong);
                boolean hasOtherActive = activeBills.stream().anyMatch(a -> !a.getHoaDon().getId().equals(id));
                if (!hasOtherActive) {
                    ban.setTrangThai(TrangThaiBan.TRONG.getValue());
                    ban.setNgayCapNhat(DateTimeUtils.now());
                    banAnRepository.save(ban);
                }
            }
            datBan.setNgayCapNhat(DateTimeUtils.now());
            datBanRepository.save(datBan);
        }

        lichSuHoaDonService.saveLichSuHoaDon(
                entity,
                "HUY_HOA_DON",
                0,
                "Hủy hóa đơn do " + request.getGhiChu()

        );


        if (tienCocHoan.compareTo(BigDecimal.ZERO) > 0) {
            lichSuThanhToanService.save(
                    entity,
                    String.valueOf(entity.getHinhThucCoc()), // hoặc entity.getHinhThucCoc()
                    tienCocHoan,
                    "Hoàn tiền cọc khi hủy hóa đơn",
                    LoaiThanhToan.HOAN_COC
            );

            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            String tienCocHoanStr = formatter.format(tienCocHoan);
            lichSuHoaDonService.saveLichSuHoaDon(
                    entity,
                    "HOAN_COC",
                    0,
                    "Hoàn " + tienCocHoanStr + " tiền cọc"

            );

        } else {
            lichSuThanhToanService.save(
                    entity,
                    String.valueOf(entity.getHinhThucCoc()),
                    tienCoc,
                    "Mất tiền cọc do hủy quá thời gian",
                    LoaiThanhToan.MAT_COC
            );

            lichSuHoaDonService.saveLichSuHoaDon(
                    entity,
                    "HOAN_COC_THAT_BAI",
                    0,
                    "Hoàn tiền cọc thất bại do do hủy quá giờ"

            );
        }
        hoaDonRepository.save(entity);
    }

    @Override
    public HoaDonResponse hoanThanhHoaDon(Long idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (!hoaDon.getTrangThai().equals(TrangThaiHoaDon.DA_THANH_TOAN.getCode())) {
            throw new RuntimeException("Chỉ được hoàn thành hóa đơn ở trạng thái đã thanh toán");
        }

        DatBan datBan = hoaDon.getDatBan();

        if (datBan != null) {

            List<Integer> trangThaiDangHoatDong = List.of(
                    TrangThaiHoaDon.VUA_TAO.getCode(),
                    TrangThaiHoaDon.CHO_COC.getCode(),
                    TrangThaiHoaDon.DA_COC.getCode(),
                    TrangThaiHoaDon.DA_XAC_NHAN.getCode(),
                    TrangThaiHoaDon.KHACH_DEN.getCode(),
                    TrangThaiHoaDon.DANG_PHUC_VU.getCode(),
                    TrangThaiHoaDon.CHO_THANH_TOAN.getCode()
            );

            List<HoaDonBanAn> dsLienKet = hoaDonBanAnRepository.findByHoaDon_Id(idHoaDon);
            for (HoaDonBanAn lk : dsLienKet) {
                BanAn ban = lk.getBanAn();
                List<HoaDonBanAn> activeBills = hoaDonBanAnRepository.findHoaDonDangHoatDongByBan(ban.getId(), trangThaiDangHoatDong);
                boolean hasOtherActive = activeBills.stream().anyMatch(a -> !a.getHoaDon().getId().equals(idHoaDon));
                if (!hasOtherActive) {
                    ban.setTrangThai(TrangThaiBan.TRONG.getValue());
                    banAnRepository.save(ban);
                }
            }
            datBan.setTrangThai(TrangThaiDatBan.HOAN_THANH.getCode());
        }

        hoaDon.setTrangThai(TrangThaiHoaDon.HOAN_THANH.getCode());
        HoaDon hoaDonSaved = hoaDonRepository.save(hoaDon);

        lichSuHoaDonService.saveLichSuHoaDon(
                hoaDonSaved,
                "HOAN_THANH",
                1,
                "Hoàn thành hóa đơn"
        );
        return hoaDonMapper.mapToResponse(hoaDonSaved);
    }

    @Override
    public TienCocPreviewResponse previewHuyHoaDon(Long id) {
        HoaDon entity = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        int thoiGianHoanCoc = thamSoHeThongService.getInt(
                ThamSoHeThong.THOI_GIAN_HUY_HOAN_COC.getCode()
        );

        BigDecimal tienCoc = entity.getTienCoc() == null
                ? BigDecimal.ZERO
                : entity.getTienCoc();

        BigDecimal tienCocHoan = BigDecimal.ZERO;
        BigDecimal tienCocBiMat = BigDecimal.ZERO;

        if (tienCoc.compareTo(BigDecimal.ZERO) > 0 && entity.getDatBan() != null) {
            LocalDateTime thoiGianDat = entity.getDatBan().getThoiGianDat();
            LocalDateTime now = LocalDateTime.now();

            long minutes = ChronoUnit.MINUTES.between(now, thoiGianDat);

            if (minutes < 0) {
                tienCocHoan = BigDecimal.ZERO;
            } else if (minutes >= thoiGianHoanCoc) {
                tienCocHoan = tienCoc;
            } else {
                tienCocHoan = BigDecimal.ZERO;
            }

            tienCocBiMat = tienCoc.subtract(tienCocHoan);
        }

        return new TienCocPreviewResponse(tienCocHoan, tienCocBiMat);
    }

    @Override
    public byte[] inHoaDon(Long id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        HoaDonResponse response = hoaDonMapper.mapToResponse(hoaDon);

        List<HoaDonChiTietResponse> chiTietList =
                hoaDonChiTietRepository.findByHoaDonId(id)
                        .stream()
                        .map(hoaDonMapper::mapToChiTietResponse)
                        .toList();

        return pdfUtil.generateHoaDonPdf(response, chiTietList);
    }

}
