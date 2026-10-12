package com.fourspicehotpot.backend.modules.banan.service.serviceImpl;

import com.fourspicehotpot.backend.modules.banan.dto.request.BanAnSerachRequest;
import com.fourspicehotpot.backend.modules.banan.dto.response.BanAnResponse;
import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.banan.entity.KhuVuc;
import com.fourspicehotpot.backend.modules.banan.mapper.KhuVucMapper;
import com.fourspicehotpot.backend.modules.banan.repository.BanAnRepository;
import com.fourspicehotpot.backend.modules.banan.service.BanAnService;
import com.fourspicehotpot.backend.modules.datban.dto.response.DatBanResponse;
import com.fourspicehotpot.backend.modules.datban.entity.DatBan;
import com.fourspicehotpot.backend.modules.datban.repository.DatBanRepository;
import com.fourspicehotpot.backend.modules.thamsohethong.service.ThamSoHeThongService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BanAnServiceImpl implements BanAnService {

    @Autowired
    private BanAnRepository banAnRepository;
    @Autowired
    private DatBanRepository datBanRepository;
    @Autowired
    private ThamSoHeThongService thamSoHeThongService;
    @Autowired
    private KhuVucMapper khuVucMapper;


    @Override
    public List<BanAn> getDanhSachBanTheoNgay(LocalDate ngay) {
        List<BanAn> danhSachBan = banAnRepository.findAllWithKhuVuc();

        LocalDateTime ngayBatDau = ngay.atStartOfDay();
        List<DatBan> danhSachDatBan = datBanRepository.findByDate(ngayBatDau);

        Map<Long, DatBan> mapBanDat = new HashMap<>();

        for (DatBan datBan : danhSachDatBan) {
            if (datBan.getBanAns() == null || datBan.getBanAns().isEmpty()) continue;

            for (BanAn ban : datBan.getBanAns()) {
                mapBanDat.put(Long.valueOf(ban.getId()), datBan);
            }
        }

        return danhSachBan.stream().map(ban -> {
            Integer trangThaiGoc = ban.getTrangThai();
            Integer trangThaiHienThi = trangThaiGoc;

            DatBan datBan = mapBanDat.get(ban.getId());

            // nếu bàn đang sử dụng, đang dọn, hoặc không sử dụng thì giữ nguyên
            if (Integer.valueOf(0).equals(trangThaiGoc) || Integer.valueOf(3).equals(trangThaiGoc) || Integer.valueOf(4).equals(trangThaiGoc)) {
                trangThaiHienThi = trangThaiGoc;
            }
            // nếu có đặt bàn trong đúng ngày thì hiển thị đã đặt
            else if (datBan != null) {
                trangThaiHienThi = 2;
            }
            // còn lại là trống
            else {
                trangThaiHienThi = 1;
            }

            return BanAn.builder()
                    .id(ban.getId())
                    .maBanAn(ban.getMaBanAn())
                    .tenBanAn(ban.getTenBanAn())
                    .sucChuaToiDa(ban.getSucChuaToiDa())
                    .trangThai(trangThaiHienThi)
                    .ngayTao(ban.getNgayTao())
                    .ngayCapNhat(ban.getNgayCapNhat())
                    .khuVuc(ban.getKhuVuc())
                    .build();
        }).toList();
    }


    @Override
    public BanAn save(BanAn banAn) {

        LocalDateTime now = LocalDateTime.now();

        // THÊM MỚI
        if (banAn.getId() == null) {

            long tongSoBan = banAnRepository
                    .countByKhuVucIdAndTrangThaiNot(
                            banAn.getKhuVuc().getId(), 0);

            if (tongSoBan >= 16) {
                throw new RuntimeException("Khu vực này chỉ được tạo tối đa 16 bàn");
            }

            String tenBan = banAn.getTenBanAn() != null
                    ? banAn.getTenBanAn().trim()
                    : "";

            banAn.setMaBanAn(tenBan);
            banAn.setNgayTao(now);
        }

        // CẬP NHẬT
        else {
            BanAn banAnCu = banAnRepository.findById(Math.toIntExact(banAn.getId()))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn ăn"));

            // Không cho đổi trạng thái nếu đang sử dụng
            if (banAnCu.getTrangThai() == 3 && banAn.getTrangThai() != 3) {
                throw new RuntimeException("Bàn đang sử dụng, không thể thay đổi trạng thái!");
            }

            // Không cho đổi nếu đã đặt
            if (banAnCu.getTrangThai() == 2 && banAn.getTrangThai() != 2) {
                throw new RuntimeException("Bàn đã đặt, không thể thay đổi trạng thái!");
            }

            banAn.setNgayTao(banAnCu.getNgayTao());
        }

        banAn.setNgayCapNhat(now);

        return banAnRepository.save(banAn);
    }

    @Override
    public BanAn findById(Integer id) {
        return banAnRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy bàn"));
    }

    @Override
    public void deleteBanAn(Integer id) {

        BanAn banAn = banAnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn để xóa"));

        // Chỉ cho xóa khi bàn trống
        if (banAn.getTrangThai() != 1) {
            throw new RuntimeException("Chỉ được xóa khi bàn đang trống");
        }

        // soft delete by updating status
        banAn.setTrangThai(com.fourspicehotpot.backend.common.enums.TrangThaiBan.KHONG_SU_DUNG.getValue());
        banAn.setNgayCapNhat(LocalDateTime.now());
        banAnRepository.save(banAn);
    }

    @Override
    public List<BanAn> findAll() {
        return banAnRepository.findAll();
    }


    @Override
    public List<BanAn> searchBanAn(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getDanhSachBanTheoNgay(LocalDate.now());
        }
        String lowerKeyword = keyword.toLowerCase();
        return getDanhSachBanTheoNgay(LocalDate.now()).stream()
                .filter(b -> (b.getTenBanAn() != null && b.getTenBanAn().toLowerCase().contains(lowerKeyword)) ||
                             (b.getMaBanAn() != null && b.getMaBanAn().toLowerCase().contains(lowerKeyword)))
                .toList();
    }

    public List<BanAnResponse> getDanhSachBan() {
        List<BanAn> dsBan = banAnRepository.findAll();

        return dsBan.stream().map(b -> {

            DatBan datBan = datBanRepository
                    .findTopByBanAns_IdAndTrangThai(Long.valueOf(b.getId()), "DA_XAC_NHAN");

            DatBanResponse datBanRes = null;

            if (datBan != null) {

                String tenKhach = null;

                if (datBan.getKhachHang() != null) {
                    tenKhach = datBan.getKhachHang().getTenKhachHang();
                }

                datBanRes = DatBanResponse.builder()
                        .id(datBan.getId())
                        .maDatBan(datBan.getMaDatBan())
                        .tenKhachHang(tenKhach)
                        .soNguoiToiDa(datBan.getSoNguoiToiDa())
                        .build();
            }

            return BanAnResponse.builder()
                    .id(Long.valueOf(b.getId()))
                    .tenBanAn(b.getTenBanAn())
                    .sucChuaToiDa(b.getSucChuaToiDa())
                    .trangThai(b.getTrangThai())
                    .datBan(datBanRes)
                    .build();

        }).toList();
    }

    @Override
    public List<BanAnResponse> getDanhSachBanChoDat(LocalDateTime thoiGianDat) {
        int thoiGianSuDung =
                thamSoHeThongService.getInt(
                        "THOI_GIAN_SU_DUNG_BAN_MAC_DINH"
                );

        LocalDateTime thoiGianKetThucDuKien =
                thoiGianDat.plusMinutes(thoiGianSuDung);

        return banAnRepository.findAll().stream().map(ban -> {

            boolean biOverlap =
                    datBanRepository.existsOverlap(
                            ban.getId(),
                            thoiGianDat,
                            thoiGianKetThucDuKien

                    );

            return BanAnResponse.builder()
                    .id(Long.valueOf(ban.getId()))
                    .maBanAn(ban.getMaBanAn())
                    .tenBanAn(ban.getTenBanAn())
                    .sucChuaToiDa(ban.getSucChuaToiDa())
                    .trangThai(ban.getTrangThai())
                    .ngayTao(ban.getNgayTao())
                    .ngayCapNhat(ban.getNgayCapNhat())
                    .khuVuc(khuVucMapper.mapToResponse(ban.getKhuVuc()))
                    .coTheDat(!biOverlap)
                    .build();

        }).toList();
    }

    @Override
    public List<BanAnResponse> filterBanAn(
            BanAnSerachRequest request,
            LocalDateTime thoiGianDat
    ) {

        int thoiGianSuDung =
                thamSoHeThongService.getInt(
                        "THOI_GIAN_SU_DUNG_BAN_MAC_DINH"
                );

        LocalDateTime thoiGianKetThucDuKien =
                thoiGianDat.plusMinutes(thoiGianSuDung);

        Specification<BanAn> spec = (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            Join<BanAn, KhuVuc> khuVucJoin = root.join("khuVuc");

            if (request.getTang() != null && !request.getTang().trim().isEmpty()) {
                predicates.add(
                        criteriaBuilder.equal(
                                khuVucJoin.get("id"),
                                request.getTang()
                        )
                );
            }

            // Số chỗ
            if (request.getSoCho() != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("sucChuaToiDa"),
                                request.getSoCho()
                        )
                );
            }

            // Trạng thái
            if (request.getTrangThai() != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("trangThai"),
                                request.getTrangThai()
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };

        return banAnRepository.findAll(spec)
                .stream()
                .map(ban -> {

                    boolean biOverlap =
                            datBanRepository.existsOverlap(
                                    ban.getId(),
                                    thoiGianDat,
                                    thoiGianKetThucDuKien
                            );

                    return BanAnResponse.builder()
                            .id(Long.valueOf(ban.getId()))
                            .maBanAn(ban.getMaBanAn())
                            .tenBanAn(ban.getTenBanAn())
                            .sucChuaToiDa(ban.getSucChuaToiDa())
                            .trangThai(ban.getTrangThai())
                            .ngayTao(ban.getNgayTao())
                            .ngayCapNhat(ban.getNgayCapNhat())
                            .khuVuc(
                                    khuVucMapper.mapToResponse(
                                            ban.getKhuVuc()
                                    )
                            )
                            .coTheDat(!biOverlap)
                            .build();

                }).toList();
    }

}
