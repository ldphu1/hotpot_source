package com.fourspicehotpot.backend.modules.dotgiamgia.service.serviceImpl;

import com.fourspicehotpot.backend.modules.dotgiamgia.service.DotGiamGiaService;
import com.fourspicehotpot.backend.common.util.DateTimeUtils;
import com.fourspicehotpot.backend.modules.dotgiamgia.dto.request.DotGiamGiaRequest;
import com.fourspicehotpot.backend.modules.dotgiamgia.dto.response.DotGiamGiaResponse;
import com.fourspicehotpot.backend.modules.dotgiamgia.entity.DotGiamGia;
import com.fourspicehotpot.backend.modules.dotgiamgia.entity.DotGiamGiaSanPham;
import com.fourspicehotpot.backend.modules.dotgiamgia.repository.DotGiamGiaRepository;
import com.fourspicehotpot.backend.modules.dotgiamgia.repository.DotGiamGiaSanPhamRepository;
import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import com.fourspicehotpot.backend.modules.setlau.repository.SanPhamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DotGiamGiaServiceImpl implements DotGiamGiaService {

    private final DotGiamGiaRepository dotGiamGiaRepository;
    private final DotGiamGiaSanPhamRepository mappingRepo;
    private final SanPhamRepository sanPhamRepository;

    // ================== CREATE ==================
    @Override
    @Transactional
    public DotGiamGiaResponse create(DotGiamGiaRequest request) {

        validateRequest(request, null);

        DotGiamGia dgg = new DotGiamGia();
        BeanUtils.copyProperties(request, dgg, "id", "isDeleted");

        dgg.setActive(true);

        dotGiamGiaRepository.save(dgg);

        saveMapping(dgg, request.getSanPhamIds());

        return toResponse(dgg);
    }

    // ================== UPDATE ==================
    @Override
    @Transactional
    public DotGiamGiaResponse update(Long id, DotGiamGiaRequest request) {

        DotGiamGia dgg = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy"));

        mappingRepo.deleteByDotGiamGia_Id(id);

        validateRequest(request, id);

        BeanUtils.copyProperties(request, dgg, "id", "isDeleted", "trangThai", "active");

        dotGiamGiaRepository.save(dgg);

        saveMapping(dgg, request.getSanPhamIds());

        return toResponse(dgg);
    }

    // ================== TOGGLE ==================
    @Override
    public void doiTrangThai(Long id) {
        DotGiamGia dgg = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy"));

        Boolean active = dgg.getActive();
        dgg.setActive(active == null || !active);

        dotGiamGiaRepository.save(dgg);
    }

    // ================== GET ALL ==================
    @Override
    public List<DotGiamGiaResponse> getAll() {
        return dotGiamGiaRepository.findByIsDeleted(0)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ================== GET BY ID ==================
    @Override
    public DotGiamGiaResponse getById(Long id) {
        DotGiamGia dgg = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        return toResponse(dgg);
    }

    // ================== DELETE ==================
    @Override
    public void delete(Long id) {
        DotGiamGia dgg = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy"));

        dgg.setIsDeleted(1);
        dotGiamGiaRepository.save(dgg);
    }

    // ================== GET ACTIVE ==================
    @Override
    public DotGiamGiaResponse getActiveBySanPham(Long sanPhamId) {

        SanPham sp = sanPhamRepository.findById(sanPhamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        List<DotGiamGia> list = dotGiamGiaRepository.findActiveBySanPham(sanPhamId);

        if (list.isEmpty()) return null;

        DotGiamGia best = list.stream()
                .max(Comparator.comparing(DotGiamGia::getGiaTriGiam))
                .orElse(null);

        return best != null ? toResponse(best) : null;
    }

    // ================== FILTER ==================
    @Override
    public List<DotGiamGiaResponse> filter(String keyword,  Integer trangThai) {

        List<DotGiamGia> list;

        if (keyword == null || keyword.trim().isEmpty()) {
            list = dotGiamGiaRepository.findByIsDeleted(0);
        } else {
            list = dotGiamGiaRepository.search(keyword);
        }

        return list.stream()
                .map(this::toResponse)
                .filter(d -> {


                    if (trangThai != null && !trangThai.equals(d.getTrangThai())) {
                        return false;
                    }

                    return true;
                })
                .toList();
    }

    // ================== HELPER ==================
    private void saveMapping(DotGiamGia dgg, List<Long> spIds) {
        if (spIds == null) return;

        for (Long spId : spIds) {
            SanPham sp = sanPhamRepository.findById(spId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            DotGiamGiaSanPham map = new DotGiamGiaSanPham();
            map.setDotGiamGia(dgg);
            map.setSanPham(sp);

            mappingRepo.save(map);
        }
    }

    private void validateRequest(DotGiamGiaRequest request, Long excludeId) {

        // Khi tạo mới, cho phép chọn thời gian bắt đầu trễ tối đa 5 phút để bù trừ độ trễ thao tác
        if (excludeId == null) {
            if (request.getNgayBatDau().isBefore(DateTimeUtils.now().minusMinutes(5))) {
                throw new RuntimeException("Ngày bắt đầu phải từ hiện tại trở đi");
            }
        }

        if (request.getNgayKetThuc().isBefore(request.getNgayBatDau())) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }



        if (request.getSanPhamIds() != null) {
            for (Long spId : request.getSanPhamIds()) {

                int count = dotGiamGiaRepository.countOverlap(
                        spId,
                        request.getNgayBatDau(),
                        request.getNgayKetThuc(),
                        excludeId
                );

                if (count > 0) {
                    throw new RuntimeException("Sản phẩm đã có giảm giá trong thời gian này");
                }
            }
        }
    }

    private BigDecimal tinhTienGiam(DotGiamGia d, BigDecimal giaGoc) {

        if (d.getGiaTriGiam() == null) return BigDecimal.ZERO;

        return giaGoc.multiply(d.getGiaTriGiam())
                .divide(BigDecimal.valueOf(100));
    }

    // ================== RESPONSE ==================
    private DotGiamGiaResponse toResponse(DotGiamGia dgg) {

        DotGiamGiaResponse res = new DotGiamGiaResponse();
        BeanUtils.copyProperties(dgg, res);

        res.setActive(dgg.getActive() != null ? dgg.getActive() : true);

        res.setSanPhams(
                mappingRepo.findByDotGiamGia_Id(dgg.getId())
                        .stream()
                        .map(x -> {
                            SanPham sp = x.getSanPham();
                            return new SanPhamResponse(
                                    sp.getId(),
                                    sp.getTenSanPham(),
                                    sp.getGia()
                            );
                        })
                        .toList()
        );

        LocalDateTime now = DateTimeUtils.now();

        if (now.isBefore(dgg.getNgayBatDau())) res.setTrangThai(0);
        else if (now.isAfter(dgg.getNgayKetThuc())) res.setTrangThai(2);
        else res.setTrangThai(1);

        return res;
    }
}
