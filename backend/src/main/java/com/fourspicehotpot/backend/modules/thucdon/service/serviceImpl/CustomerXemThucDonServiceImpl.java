package com.fourspicehotpot.backend.modules.thucdon.service.serviceImpl;

import com.fourspicehotpot.backend.modules.setlau.mapper.CustomerSanPhamMapper;
import com.fourspicehotpot.backend.modules.thucdon.repository.SanPhamCustomerRepository;
import com.fourspicehotpot.backend.modules.thucdon.service.CustomerXemThucDonService;
import com.fourspicehotpot.backend.modules.dinhluong.entity.DinhLuong;
import com.fourspicehotpot.backend.modules.setlau.dto.request.SanPhamSearchRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import com.fourspicehotpot.backend.modules.setlau.repository.SanPhamRepository;
import com.fourspicehotpot.backend.modules.thucdon.dto.response.DanhMucResponse;
import com.fourspicehotpot.backend.modules.thucdon.repository.DanhMucRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerXemThucDonServiceImpl implements CustomerXemThucDonService {

    @Autowired
    private SanPhamRepository sanPhamRepository;
    @Autowired
    private DanhMucRepository danhMucRepository;
    @Autowired
    private SanPhamCustomerRepository sanPhamCustomerRepository;
    @Autowired
    private CustomerSanPhamMapper sanPhamMapper;

    @Override
    public List<SanPhamResponse> getAllSanPham() {
        return sanPhamRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .filter(sp -> !sp.getDanhMuc().getId().equals(6)) // bỏ danh mục lẩu
                .map(sp -> {
                    DinhLuong dl = sp.getDinhLuong();
                    return SanPhamResponse.builder()
                            .id(sp.getId())
                            .maSanPham(sp.getMaSanPham())
                            .tenSanPham(sp.getTenSanPham())
                            .gia(sp.getGia())
                            .moTa(sp.getMoTa())
                            .trangThaiXoa(sp.getTrangThaiXoa())
                            .idDanhMuc(sp.getDanhMuc().getId())
                            .tenDanhMuc(sp.getDanhMuc().getTenDanhMuc())
                            .idDinhLuong(dl != null ? dl.getId() : null)
                            .idDonVi(dl != null && dl.getDonVi() != null ? dl.getDonVi().getId() : null)
                            .giaTri(dl != null ? dl.getGiaTri() : null)
                            .tenDonVi(dl != null && dl.getDonVi() != null
                                    ? dl.getDonVi().getTenDonVi()
                                    : null)
                            .hinhAnh(sp.getHinhAnh())
                            .build();
                })
                .toList();
    }

    @Override
    public List<SanPhamResponse> getAllMonLe() {
        return sanPhamRepository.findByLaSetFalseAndTrangThaiXoaFalse().stream().map(sanPhamMapper::mapToResponse).toList();
    }

    @Override
    public SanPhamResponse findById(Integer id) {
        SanPham sp = sanPhamRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        DinhLuong dl = sp.getDinhLuong();
        return SanPhamResponse.builder()
                .id(sp.getId())
                .maSanPham(sp.getMaSanPham())
                .tenSanPham(sp.getTenSanPham())
                .gia(sp.getGia())
                .moTa(sp.getMoTa())
                .trangThaiXoa(sp.getTrangThaiXoa())
                .idDanhMuc(sp.getDanhMuc().getId())
                .tenDanhMuc(sp.getDanhMuc().getTenDanhMuc())
                .idDinhLuong(dl != null ? dl.getId() : null)
                .idDonVi(dl != null && dl.getDonVi() != null ? dl.getDonVi().getId() : null)
                .giaTri(dl != null ? dl.getGiaTri() : null)
                .tenDonVi(dl != null && dl.getDonVi() != null
                        ? dl.getDonVi().getTenDonVi()
                        : null)

                .hinhAnh(sp.getHinhAnh())
                .build();
    }

    @Override
    public List<SanPhamResponse> findSanPhamByDanhMuc(Integer id) {
        return sanPhamRepository.findSanPhamByDanhMuc(id)
                .stream()
                .map(sanPhamMapper::mapToResponse)
                .toList();
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
                .map(sanPhamMapper::mapToResponse)
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
            return res;
        }).toList();
    }

    @Override
    public List<SanPhamResponse> searchSetLauByNuocLau(Long idNuocLau) {

        Specification<SanPham> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (idNuocLau != null) {
                predicates.add(
                        criteriaBuilder.and(
                                criteriaBuilder.equal(
                                        root.get("nuocLau").get("id"), idNuocLau
                                )
                        )
                );
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return sanPhamCustomerRepository.findAll(spec).stream().map(sanPhamMapper::mapToResponse).toList();
    }


    @Override
    public List<DanhMucResponse> findAllDanhMucHoatDong() {
        return danhMucRepository.findAllDanhMucHoatDong()
                .stream()
                .map(danhMuc -> DanhMucResponse.builder()
                        .id(danhMuc.getId())
                        .maDanhMuc(danhMuc.getMaDanhMuc())
                        .tenDanhMuc(danhMuc.getTenDanhMuc())
                        .trangThaiXoa(danhMuc.getTrangThaiXoa())
                        .soLuongMon(danhMuc.getSoLuongMon())
                        .build()
                )
                .collect(Collectors.toList());
    }


}
