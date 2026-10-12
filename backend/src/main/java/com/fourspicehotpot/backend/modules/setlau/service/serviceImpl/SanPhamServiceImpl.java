package com.fourspicehotpot.backend.modules.setlau.service.serviceImpl;

import com.fourspicehotpot.backend.common.util.DateTimeUtils;
import com.fourspicehotpot.backend.modules.dinhluong.entity.DinhLuong;
import com.fourspicehotpot.backend.modules.dinhluong.repository.DinhLuongRepository;
import com.fourspicehotpot.backend.modules.dotgiamgia.entity.DotGiamGia;
import com.fourspicehotpot.backend.modules.dotgiamgia.entity.DotGiamGiaSanPham;
import com.fourspicehotpot.backend.modules.dotgiamgia.repository.DotGiamGiaSanPhamRepository;
import com.fourspicehotpot.backend.modules.setlau.dto.request.SanPhamRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.request.SanPhamSearchRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import com.fourspicehotpot.backend.modules.setlau.mapper.SanPhamMapper;
import com.fourspicehotpot.backend.modules.setlau.repository.SanPhamRepository;
import com.fourspicehotpot.backend.modules.setlau.service.SanPhamService;
import com.fourspicehotpot.backend.modules.thucdon.entity.DanhMuc;
import com.fourspicehotpot.backend.modules.thucdon.repository.DanhMucRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SanPhamServiceImpl implements SanPhamService {
    @Autowired
    private SanPhamRepository sanPhamRepository;
    @Autowired
    private DanhMucRepository danhMucRepository;
    @Autowired
    private DinhLuongRepository dinhLuongRepository;
    @Autowired
    private SanPhamMapper sanPhamMapper;
    @Autowired
    private DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;

    private SanPhamResponse mapToResponse(SanPham sp) {

        return SanPhamResponse.builder()
                .id(sp.getId())
                .maSanPham(sp.getMaSanPham())
                .tenSanPham(sp.getTenSanPham())
                .gia(sp.getGia())
                .moTa(sp.getMoTa())
                .hinhAnh(sp.getHinhAnh())
                .laSet(sp.getLaSet())
                .trangThaiXoa(sp.getTrangThaiXoa())
                .idDanhMuc(
                        sp.getDanhMuc() != null ? sp.getDanhMuc().getId() : null
                )
                .tenDanhMuc(
                        sp.getDanhMuc() != null ? sp.getDanhMuc().getTenDanhMuc() : null
                )

                .idDinhLuong(
                        sp.getDinhLuong() != null ? sp.getDinhLuong().getId() : null
                )
                .giaTri(
                        sp.getDinhLuong() != null ? sp.getDinhLuong().getGiaTri() : null
                )
                .idDonVi(
                        sp.getDinhLuong() != null &&
                                sp.getDinhLuong().getDonVi() != null
                                ? sp.getDinhLuong().getDonVi().getId()
                                : null
                )
                .tenDonVi(
                        sp.getDinhLuong() != null &&
                                sp.getDinhLuong().getDonVi() != null
                                ? sp.getDinhLuong().getDonVi().getTenDonVi()
                                : null
                )
                .build();
    }

    @Override
    public List<SanPhamResponse> getAllSanPham() {
        return sanPhamRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .filter(sp -> !sp.getDanhMuc().getId().equals(1)) // bỏ danh mục lẩu
                .map(sanPhamMapper::mapToResponse)
                .toList();
    }

    @Override
    public List<SanPhamResponse> getAllMonLe() {
        return sanPhamRepository.findByLaSetFalseAndTrangThaiXoaFalse().stream().map(this::mapToResponse).toList();
    }

    @Override
    public SanPhamResponse findById(Integer id) {
        SanPham sp = sanPhamRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        return sanPhamMapper.mapToResponse(sp);
    }


    @Override
    public List<SanPhamResponse> findSanPhamByDanhMuc(Integer id) {
        return sanPhamRepository.findSanPhamByDanhMuc(id)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<SanPhamResponse> getAllSetLau() {

        List<SanPham> sets = sanPhamRepository.getAllSetLau();

        return sets.stream().map(sp -> {
            SanPhamResponse res = new SanPhamResponse();
            res.setId(sp.getId());
            res.setMaSanPham(sp.getMaSanPham());
            res.setTenSanPham(sp.getTenSanPham());
            res.setGia(sp.getGia());
            res.setIdDanhMuc(sp.getDanhMuc().getId());
            res.setTenDanhMuc(sp.getDanhMuc().getTenDanhMuc());
            res.setHinhAnh(sp.getHinhAnh());
            res.setMoTa(sp.getMoTa());
            res.setTrangThaiXoa(sp.getTrangThaiXoa());
            res.setLaSet(sp.getLaSet());
            return res;
        }).toList();
    }

    @Transactional
    @Override
    public void addSanPham(List<SanPhamRequest> requests, MultipartFile file) {

        String fileName = null;
        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = "/app/uploads/";

                File folder = new File(uploadDir);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                File dest = new File(uploadDir + fileName);

                file.transferTo(dest);

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Upload ảnh thất bại: " + e.getMessage());
            }
        }
        for (SanPhamRequest dto : requests) {

            DanhMuc danhMuc = danhMucRepository
                    .findById(dto.getIdDanhMuc())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

            DinhLuong dinhLuong = dinhLuongRepository
                    .findById(Long.valueOf(dto.getIdDinhLuong()))
                    .orElseThrow(() -> new RuntimeException("Định lượng không tồn tại"));

            SanPham sanPham = new SanPham();
            sanPham.setTenSanPham(dto.getTenSanPham());
            sanPham.setGia(dto.getGia());
            sanPham.setMoTa(dto.getMoTa());
            sanPham.setDanhMuc(danhMuc);


            sanPham.setDinhLuong(dinhLuong);

            sanPham.setHinhAnh(fileName);
            sanPham.setLaSet(false);
            sanPham.setTrangThai(true);
            sanPham.setTrangThaiXoa(false);

            sanPhamRepository.save(sanPham);
        }
    }

    @Transactional
    @Override
    public void updateSanPham(Integer id, SanPhamRequest dto, MultipartFile file) {

        SanPham sanPham = sanPhamRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // ===== Upload ảnh nếu có =====
        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = "/app/uploads/";

                File folder = new File(uploadDir);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                File dest = new File(uploadDir + fileName);
                file.transferTo(dest);

                sanPham.setHinhAnh(fileName);

            } catch (IOException e) {
                throw new RuntimeException("Upload ảnh thất bại: " + e.getMessage());
            }
        }

        // ===== Update danh mục =====
        DanhMuc danhMuc = danhMucRepository
                .findById(dto.getIdDanhMuc())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        // ===== Update định lượng =====
        DinhLuong dinhLuong = dinhLuongRepository
                .findById(Long.valueOf(dto.getIdDinhLuong()))
                .orElseThrow(() -> new RuntimeException("Định lượng không tồn tại"));

        // ===== Set lại dữ liệu =====
        sanPham.setTenSanPham(dto.getTenSanPham());
        sanPham.setGia(dto.getGia());
        sanPham.setMoTa(dto.getMoTa());
        sanPham.setDanhMuc(danhMuc);
        sanPham.setDinhLuong(dinhLuong);

        sanPhamRepository.save(sanPham);
    }

    @Override
    public List<SanPhamResponse> searchSetLau(
            String keyword,
            Long idNuocLau,
            Boolean trangThaiXoa,
            Integer minPrice,
            Integer maxPrice) {

        List<SanPham> list = sanPhamRepository.searchSetLau(
                keyword,
                idNuocLau,
                trangThaiXoa,
                minPrice,
                maxPrice
        );

        return list.stream().map(sp -> SanPhamResponse.builder()
                .id(sp.getId())
                .maSanPham(sp.getMaSanPham())
                .tenSanPham(sp.getTenSanPham())
                .gia(sp.getGia())
                .moTa(sp.getMoTa())
                .trangThaiXoa(sp.getTrangThaiXoa())
                .laSet(sp.getLaSet())

                // Danh mục
                .idDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getId() : null)
                .tenDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getTenDanhMuc() : null)

                // Nước lẩu
                .idNuocLau(sp.getNuocLau() != null ? sp.getNuocLau().getId() : null)
                .tenNuocLau(sp.getNuocLau() != null ? sp.getNuocLau().getTenNuocLau() : null)
                .giaPhuThu(sp.getNuocLau() != null ? sp.getNuocLau().getGiaPhuThu() : null)

                // Định lượng
                .idDinhLuong(sp.getDinhLuong() != null ? sp.getDinhLuong().getId() : null)
                .giaTri(sp.getDinhLuong() != null ? sp.getDinhLuong().getGiaTri() : null)

                // Đơn vị
                .idDonVi(
                        sp.getDinhLuong() != null && sp.getDinhLuong().getDonVi() != null
                                ? sp.getDinhLuong().getDonVi().getId()
                                : null
                )
                .tenDonVi(
                        sp.getDinhLuong() != null && sp.getDinhLuong().getDonVi() != null
                                ? sp.getDinhLuong().getDonVi().getTenDonVi()
                                : null
                )

                // Hình ảnh
                .hinhAnh(sp.getHinhAnh())

                .build()
        ).toList();
    }

    @Override
    public List<SanPhamResponse> searchSanPham(SanPhamSearchRequest request) {

        Specification<SanPham> spec = (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // keyword
            if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("tenSanPham")),
                                "%" + request.getKeyword().toLowerCase() + "%"
                        )
                );
            }

            // danh mục
            if (request.getIdDanhMuc() != null) {
                predicates.add(
                        cb.equal(root.get("danhMuc").get("id"), request.getIdDanhMuc())
                );
            }

            // trạng thái
            if (request.getTrangThaiXoa() != null) {
                predicates.add(
                        cb.equal(root.get("trangThaiXoa"), request.getTrangThaiXoa())
                );
            }

            // giá từ
            if (request.getMinPrice() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("gia"), request.getMinPrice())
                );
            }

            // giá đến
            if (request.getMaxPrice() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("gia"), request.getMaxPrice())
                );
            }

            // bỏ danh mục lẩu giống getAllSanPham
            predicates.add(
                    cb.notEqual(root.get("danhMuc").get("id"), 1)
            );

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.unsorted();

        if ("asc".equalsIgnoreCase(request.getSortGia())) {
            sort = Sort.by("gia").ascending();
        }

        if ("desc".equalsIgnoreCase(request.getSortGia())) {
            sort = Sort.by("gia").descending();
        }

        List<SanPham> list = sanPhamRepository.findAll(spec, sort);

        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void chuyenTrangThai(Integer id) {
        Optional<SanPham> optional = sanPhamRepository.findById(Long.valueOf(id));
        SanPham sp = optional.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        sp.setTrangThaiXoa(!Boolean.TRUE.equals(sp.getTrangThaiXoa()));
        sanPhamRepository.save(sp);

    }

    @Override
    public List<SanPhamResponse> getSetLau() {
        return sanPhamRepository
                .findByLaSetTrueAndTrangThaiXoaFalseOrderByIdDesc()
                .stream()
                .map(sp -> SanPhamResponse.builder()
                        .id(sp.getId())
                        .maSanPham(sp.getMaSanPham())
                        .tenSanPham(sp.getTenSanPham())
                        .gia(sp.getGia())
                        .moTa(sp.getMoTa())
                        .hinhAnh(sp.getHinhAnh())
                        .laSet(sp.getLaSet())
                        .trangThaiXoa(sp.getTrangThaiXoa())

                        .idDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getId() : null)
                        .tenDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getTenDanhMuc() : null)

                        .build()
                )
                .toList();
    }

    @Override
    public List<SanPhamResponse> getSanPhamDotGiamGia() {

        List<Object[]> list = sanPhamRepository.getSanPhamClient();

        return list.stream().map(o -> {
            SanPhamResponse res = new SanPhamResponse();

            res.setId(o[0] != null ? ((Number) o[0]).longValue() : null);
            res.setTenSanPham(o[1] != null ? (String) o[1] : null);

            BigDecimal gia = o[2] != null
                    ? new BigDecimal(((Number) o[2]).toString())
                    : BigDecimal.ZERO;

            Boolean laSet = (o[3] instanceof Boolean)
                    ? (Boolean) o[3]
                    : o[3] != null && ((Number) o[3]).intValue() == 1;

            BigDecimal giaTriGiam = o[4] != null
                    ? new BigDecimal(((Number) o[4]).toString())
                    : null;

            BigDecimal soTienGiam = o[5] != null
                    ? new BigDecimal(((Number) o[5]).toString())
                    : null;

            res.setGia(gia);
            res.setLaSet(laSet);
            res.setHinhAnh(o[6] != null ? (String) o[6] : null);
            res.setTenDanhMuc(o[7] != null ? (String) o[7] : null);

            if (soTienGiam != null && soTienGiam.compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal giaSau = gia.subtract(soTienGiam);

                if (giaSau.compareTo(BigDecimal.ZERO) < 0) {
                    giaSau = BigDecimal.ZERO;
                }

                res.setGiaSauGiam(giaSau);

                // Chỉ còn giaTriGiam nên lưu lại nếu FE cần
                res.setSoTienGiam(soTienGiam);

            } else {
                res.setGiaSauGiam(gia);
                res.setSoTienGiam(null);
            }

            return res;

        }).toList();
    }

    @Override
    public List<SanPhamResponse> getAllSanPhamDgg() {
        return sanPhamRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(sanPhamMapper::mapToResponse)
                .toList();
    }

    @Override
    public List<SanPhamResponse> getSanPhamDotGiamGiaByDanhmuc(Integer idDanhMuc) {
        List<Object[]> list = sanPhamRepository.getSanPhamClientByDanhMuc(idDanhMuc);

        return list.stream().map(o -> {
            SanPhamResponse res = new SanPhamResponse();

            res.setId(o[0] != null ? ((Number) o[0]).longValue() : null);
            res.setTenSanPham(o[1] != null ? (String) o[1] : null);

            BigDecimal gia = o[2] != null
                    ? new BigDecimal(((Number) o[2]).toString())
                    : BigDecimal.ZERO;

            Boolean laSet = (o[3] instanceof Boolean)
                    ? (Boolean) o[3]
                    : o[3] != null && ((Number) o[3]).intValue() == 1;

            BigDecimal giaTriGiam = o[4] != null
                    ? new BigDecimal(((Number) o[4]).toString())
                    : null;

            BigDecimal soTienGiamThucTe = o[5] != null
                    ? new BigDecimal(((Number) o[5]).toString())
                    : null;

            res.setGia(gia);
            res.setLaSet(laSet);

            res.setIdDanhMuc(o[6] != null ? ((Number) o[6]).intValue() : null);
            res.setHinhAnh(o[7] != null ? (String) o[7] : null);
            res.setTenDanhMuc(o[8] != null ? (String) o[8] : null);

            if (soTienGiamThucTe != null
                    && soTienGiamThucTe.compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal giaSau = gia.subtract(soTienGiamThucTe);

                if (giaSau.compareTo(BigDecimal.ZERO) < 0) {
                    giaSau = BigDecimal.ZERO;
                }

                res.setGiaSauGiam(giaSau);

                // nếu chỉ còn 1 field giaTriGiam
                res.setSoTienGiam(soTienGiamThucTe);

                // hoặc nếu FE đang dùng phanTramGiam thì map:
                // res.setPhanTramGiam(giaTriGiam != null ? giaTriGiam.intValue() : null);

            } else {
                res.setGiaSauGiam(gia);
                res.setSoTienGiam(null);
                res.setPhanTramGiam(null);
            }

            return res;
        }).toList();
    }

    @Override
    public List<SanPhamResponse> getSanPhamTheoLoai(Boolean laSet) {
        return sanPhamRepository.getSanPhamClientByLoai(laSet)
                .stream()
                .map(sanPhamMapper::mapFromObjectArray)
                .toList();
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
