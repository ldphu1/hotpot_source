package com.fourspicehotpot.backend.modules.phieugiamgia.service.serviceImpl;

import com.fourspicehotpot.backend.integration.mail.service.MailService;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.khachhang.repository.KhachHangRepository;
import com.fourspicehotpot.backend.modules.phieugiamgia.dto.request.ThemPhieuGiamGiaRequest;
import com.fourspicehotpot.backend.modules.phieugiamgia.entity.KhachHangGiamGia;
import com.fourspicehotpot.backend.modules.phieugiamgia.entity.PhieuGiamGia;
import com.fourspicehotpot.backend.modules.phieugiamgia.repository.KhachHangGiamGiaRepository;
import com.fourspicehotpot.backend.modules.phieugiamgia.repository.PhieuGiamGiaRepository;
import com.fourspicehotpot.backend.modules.phieugiamgia.service.PhieuGiamGiaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PhieuGiamGiaServiceImpl implements PhieuGiamGiaService {

    NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private KhachHangGiamGiaRepository khachHangGiamGiaRepository;

    @Autowired
    private MailService mailService;

    private String genMaGiamGia() {
        return "PGG-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    @Override
    public List<PhieuGiamGia> findAllPhieuGiamGia() {
        return phieuGiamGiaRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Transactional
    @Override
    public void addPhieuGiamGia(ThemPhieuGiamGiaRequest req) {

        LocalDateTime now = LocalDateTime.now();

        if (req.getKieuGiamGia() == 2 &&
                (req.getDanhSachKhachHangIds() == null
                        || req.getDanhSachKhachHangIds().isEmpty())) {
            throw new RuntimeException("Phiếu giảm giá riêng tư phải chọn khách hàng");
        }

        if (req.getPhuongThucGiamGia() == 1 &&
                req.getGiaTriGiam().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new RuntimeException("Giảm % không được lớn hơn 100%");
        }

        PhieuGiamGia entity = new PhieuGiamGia();

        entity.setMaGiamGia(
                req.getMaGiamGia() != null && !req.getMaGiamGia().isBlank()
                        ? req.getMaGiamGia()
                        : genMaGiamGia()
        );

        entity.setTenGiamGia(req.getTenGiamGia());
        entity.setSoLuong(req.getSoLuong());
        entity.setKieuGiamGia(req.getKieuGiamGia());
        entity.setPhuongThucGiamGia(req.getPhuongThucGiamGia());
        entity.setDieuKienSuDung(req.getDieuKienSuDung());
        entity.setGiaTriGiam(req.getGiaTriGiam());
        entity.setGiamToiDa(req.getGiamToiDa());
        entity.setNgayBatDau(req.getNgayBatDau());
        entity.setNgayKetThuc(req.getNgayKetThuc());

        entity.setNgayTao(now);
        entity.setNgayCapNhat(now);
        entity.setTaoBoi(0L);
        entity.setCapNhatBoi(0L);
        entity.setTrangThaiXoa(false);

        phieuGiamGiaRepository.save(entity);

        if (req.getKieuGiamGia() == 2) {

            List<KhachHang> lstKhachHang =
                    khachHangRepository.findAllById(req.getDanhSachKhachHangIds());

            for (KhachHang kh : lstKhachHang) {

                KhachHangGiamGia map = new KhachHangGiamGia();
                map.setKhachHang(kh);
                map.setPhieuGiamGia(entity);
                map.setNgayTao(now);

                khachHangGiamGiaRepository.save(map);

                mailService.sendMailNewVoucher(entity, kh);
            }
        }
    }

    @Override
    public Map<String, Object> findPhieuGiamGiaById(Integer id) {

        PhieuGiamGia entity = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phiếu giảm giá"));

        List<KhachHangGiamGia> mappings =
                khachHangGiamGiaRepository.findByPhieuGiamGia(entity);

        List<Long> khIds = mappings.stream()
                .map(m -> m.getKhachHang().getId())
                .collect(Collectors.toList());

        Map<String, Object> res = new HashMap<>();

        res.put("id", entity.getId());
        res.put("tenGiamGia", entity.getTenGiamGia());
        res.put("soLuong", entity.getSoLuong());
        res.put("kieuGiamGia", entity.getKieuGiamGia());
        res.put("phuongThucGiamGia", entity.getPhuongThucGiamGia());
        res.put("giaTriGiam", entity.getGiaTriGiam());
        res.put("dieuKienSuDung", entity.getDieuKienSuDung());
        res.put("giamToiDa", entity.getGiamToiDa());
        res.put("ngayBatDau", entity.getNgayBatDau());
        res.put("ngayKetThuc", entity.getNgayKetThuc());
        res.put("danhSachKhachHangIds", khIds);

        return res;
    }


    @Transactional
    @Override
    public void updatePhieuGiamGia(Integer id, ThemPhieuGiamGiaRequest req) {

        LocalDateTime now = LocalDateTime.now();

        PhieuGiamGia entity = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phiếu giảm giá"));

        entity.setTenGiamGia(req.getTenGiamGia());

        if (req.getKieuGiamGia() == 2) {
            entity.setSoLuong(req.getDanhSachKhachHangIds().size());
        } else {
            entity.setSoLuong(req.getSoLuong());
        }

        entity.setKieuGiamGia(req.getKieuGiamGia());
        entity.setPhuongThucGiamGia(req.getPhuongThucGiamGia());
        entity.setDieuKienSuDung(req.getDieuKienSuDung());
        entity.setGiaTriGiam(req.getGiaTriGiam());
        entity.setGiamToiDa(req.getGiamToiDa());
        entity.setNgayBatDau(req.getNgayBatDau());
        entity.setNgayKetThuc(req.getNgayKetThuc());
        entity.setNgayCapNhat(now);

        phieuGiamGiaRepository.save(entity);

        if (req.getKieuGiamGia() != 2) {
            khachHangGiamGiaRepository.deleteByPhieuGiamGia(entity);
            return;
        }

        List<Long> newIds = req.getDanhSachKhachHangIds();

        List<KhachHangGiamGia> oldMappings =
                khachHangGiamGiaRepository.findByPhieuGiamGia(entity);

        Set<Long> oldIds = oldMappings.stream()
                .map(m -> m.getKhachHang().getId())
                .collect(Collectors.toSet());

        Set<Long> newIdSet = new HashSet<>(newIds);

        List<Long> addedIds = newIds.stream()
                .filter(idKh -> !oldIds.contains(idKh))
                .toList();

        List<Long> removedIds = oldIds.stream()
                .filter(idKh -> !newIdSet.contains(idKh))
                .toList();

        List<Long> existingIds = newIds.stream()
                .filter(oldIds::contains)
                .toList();

        if (!removedIds.isEmpty()) {

            khachHangGiamGiaRepository.deleteByPhieuGiamGiaAndKhachHangIdIn(
                    entity.getId(),
                    removedIds
            );
        }

        if (!addedIds.isEmpty()) {

            List<KhachHang> newCustomers =
                    khachHangRepository.findAllById(addedIds);

            for (KhachHang kh : newCustomers) {

                KhachHangGiamGia map = new KhachHangGiamGia();
                map.setKhachHang(kh);
                map.setPhieuGiamGia(entity);
                map.setNgayTao(now);

                khachHangGiamGiaRepository.save(map);

                mailService.sendMailNewVoucher(entity, kh);
            }
        }

        if (!existingIds.isEmpty()) {

            List<KhachHang> oldCustomers =
                    khachHangRepository.findAllById(existingIds);

            for (KhachHang kh : oldCustomers) {

                mailService.sendMailUpdateVoucher(entity, kh);
            }
        }
    }

    @Override
    public void deletePhieuGiamGia(Integer id) {

        PhieuGiamGia entity = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phiếu giảm giá"));

        entity.setTrangThaiXoa(true);
        entity.setNgayCapNhat(LocalDateTime.now());

        phieuGiamGiaRepository.save(entity);
    }

    @Override
    public void restorePhieuGiamGia(Integer id) {

        PhieuGiamGia entity = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phiếu giảm giá"));

        entity.setTrangThaiXoa(false);
        entity.setNgayCapNhat(LocalDateTime.now());

        phieuGiamGiaRepository.save(entity);
    }

    @Override
    public Page<PhieuGiamGia> filterPhieuGiamGia(String keyword, Integer phuongThucGiamGia,
                                                 Integer kieuGiamGia, Integer trangThai,
                                                 Pageable pageable) {

        return phieuGiamGiaRepository.findAll((root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {

                Predicate ten = cb.like(
                        cb.lower(root.get("tenGiamGia")),
                        "%" + keyword.toLowerCase() + "%"
                );

                Predicate ma = cb.like(
                        cb.lower(root.get("maGiamGia")),
                        "%" + keyword.toLowerCase() + "%"
                );

                predicates.add(cb.or(ten, ma));
            }

            if (phuongThucGiamGia != null) {
                predicates.add(cb.equal(root.get("phuongThucGiamGia"), phuongThucGiamGia));
            }

            if (kieuGiamGia != null) {
                predicates.add(cb.equal(root.get("kieuGiamGia"), kieuGiamGia));
            }

            if (trangThai != null) {

                LocalDateTime now = LocalDateTime.now();

                if (trangThai == 0) {
                    predicates.add(cb.greaterThan(root.get("ngayBatDau"), now));
                } else if (trangThai == 1) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("ngayBatDau"), now));
                    predicates.add(cb.greaterThanOrEqualTo(root.get("ngayKetThuc"), now));
                } else if (trangThai == 2) {
                    predicates.add(cb.lessThan(root.get("ngayKetThuc"), now));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));

        }, pageable);
    }
}