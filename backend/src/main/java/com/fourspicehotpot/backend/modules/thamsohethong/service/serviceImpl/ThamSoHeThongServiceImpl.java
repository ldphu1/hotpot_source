package com.fourspicehotpot.backend.modules.thamsohethong.service.serviceImpl;

import com.fourspicehotpot.backend.common.enums.ThamSoHeThong;
import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.datban.entity.DatBan;
import com.fourspicehotpot.backend.modules.thamsohethong.dto.request.ThamSoHeThongRequest;
import com.fourspicehotpot.backend.modules.thamsohethong.dto.response.ThamSoHeThongResponse;
import com.fourspicehotpot.backend.modules.thamsohethong.repository.ThamSoHeThongRepository;
import com.fourspicehotpot.backend.modules.thamsohethong.service.ThamSoHeThongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThamSoHeThongServiceImpl implements ThamSoHeThongService {
    @Autowired
    private ThamSoHeThongRepository thamSoHeThongRepository;

    private String generateMaThamSo() {
        long number = System.currentTimeMillis() % 1_000_000_000L; // 9 số
        return String.format("TS-%09d", number);
    }

    public com.fourspicehotpot.backend.modules.thamsohethong.entity.ThamSoHeThong mapToEntity(ThamSoHeThongRequest req) {
        return com.fourspicehotpot.backend.modules.thamsohethong.entity.ThamSoHeThong.builder()
                .maThamSo(
                        (req.getMaThamSo() == null || req.getMaThamSo().isBlank())
                                ? generateMaThamSo()
                                : req.getMaThamSo()
                )
                .tenThamSo(req.getTenThamSo())
                .giaTri(req.getGiaTri())
                .kieuDuLieu(req.getKieuDuLieu())
                .nhom(req.getNhom())
                .moTa(req.getMoTa())
                .trangThai(req.getTrangThai() != null ? req.getTrangThai() : true)
                .build();
    }

    public ThamSoHeThongResponse mapToResponse(com.fourspicehotpot.backend.modules.thamsohethong.entity.ThamSoHeThong entity) {
        return ThamSoHeThongResponse.builder()
                .id(entity.getId())
                .maThamSo(entity.getMaThamSo())
                .tenThamSo(entity.getTenThamSo())
                .giaTri(entity.getGiaTri())
                .kieuDuLieu(entity.getKieuDuLieu())
                .nhom(entity.getNhom())
                .moTa(entity.getMoTa())
                .trangThai(entity.getTrangThai())
                .ngayTao(entity.getNgayTao())
                .ngayCapNhat(entity.getNgayCapNhat())
                .build();
    }

    @Override
    public List<ThamSoHeThongResponse> getAllThamSo() {
        return thamSoHeThongRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Cacheable(value = "thamSo", key = "#code")
    @Override
    public String get(String code) {
        com.fourspicehotpot.backend.modules.thamsohethong.entity.ThamSoHeThong ts = thamSoHeThongRepository.findByMaThamSoAndTrangThaiTrue(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tham số: " + code));

        return ts.getGiaTri();
    }

    @Override
    public Integer getInt(String code) {
        com.fourspicehotpot.backend.modules.thamsohethong.entity.ThamSoHeThong ts = getEntity(code);

        if (!"INT".equalsIgnoreCase(ts.getKieuDuLieu())) {
            throw new RuntimeException("Sai kiểu dữ liệu INT: " + code);
        }

        return Integer.parseInt(ts.getGiaTri());
    }

    @Override
    public Double getDouble(String code) {
        com.fourspicehotpot.backend.modules.thamsohethong.entity.ThamSoHeThong ts = getEntity(code);

        if (!"DOUBLE".equalsIgnoreCase(ts.getKieuDuLieu())) {
            throw new RuntimeException("Sai kiểu dữ liệu DOUBLE: " + code);
        }

        return Double.parseDouble(ts.getGiaTri());
    }

    @Override
    public ThamSoHeThongResponse getDetail(Long id) {
        com.fourspicehotpot.backend.modules.thamsohethong.entity.ThamSoHeThong entity = thamSoHeThongRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tham số hệ thống với id: " + id));

        return mapToResponse(entity);
    }

    @Override
    @CacheEvict(value = "thamSo", allEntries = true)
    public ThamSoHeThongResponse create(ThamSoHeThongRequest request) {
        validateRequest(request);

        String maThamSo = (request.getMaThamSo() == null || request.getMaThamSo().isBlank())
                ? generateMaThamSo()
                : request.getMaThamSo().trim();

        if (thamSoHeThongRepository.existsByMaThamSo(maThamSo)) {
            throw new RuntimeException("Mã tham số đã tồn tại: " + maThamSo);
        }

        com.fourspicehotpot.backend.modules.thamsohethong.entity.ThamSoHeThong entity = com.fourspicehotpot.backend.modules.thamsohethong.entity.ThamSoHeThong.builder()
                .maThamSo(maThamSo)
                .tenThamSo(request.getTenThamSo().trim())
                .giaTri(request.getGiaTri().trim())
                .kieuDuLieu(request.getKieuDuLieu().trim())
                .nhom(request.getNhom())
                .moTa(request.getMoTa())
                .trangThai(request.getTrangThai() != null ? request.getTrangThai() : true)
                .build();

        return mapToResponse(thamSoHeThongRepository.save(entity));
    }

    @Override
    @CacheEvict(value = "thamSo", allEntries = true)
    public ThamSoHeThongResponse update(Long id, ThamSoHeThongRequest request) {
        validateRequest(request);

        com.fourspicehotpot.backend.modules.thamsohethong.entity.ThamSoHeThong entity = thamSoHeThongRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tham số hệ thống với id: " + id));

        String maThamSo = (request.getMaThamSo() == null || request.getMaThamSo().isBlank())
                ? entity.getMaThamSo()
                : request.getMaThamSo().trim();

        if (thamSoHeThongRepository.existsByMaThamSoAndIdNot(maThamSo, id)) {
            throw new RuntimeException("Mã tham số đã tồn tại: " + maThamSo);
        }

        entity.setMaThamSo(maThamSo);
        entity.setTenThamSo(request.getTenThamSo().trim());
        entity.setGiaTri(request.getGiaTri().trim());
        entity.setKieuDuLieu(request.getKieuDuLieu().trim());
        entity.setNhom(request.getNhom());
        entity.setMoTa(request.getMoTa());
        entity.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : entity.getTrangThai());

        return mapToResponse(thamSoHeThongRepository.save(entity));
    }

    @Override
    @CacheEvict(value = "thamSo", allEntries = true)
    public void delete(Long id) {
        com.fourspicehotpot.backend.modules.thamsohethong.entity.ThamSoHeThong entity = thamSoHeThongRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tham số hệ thống với id: " + id));

        entity.setTrangThai(!entity.getTrangThai());
        thamSoHeThongRepository.save(entity);
    }

    @Override
    public void validateSucChuaBan(DatBan datBan, List<BanAn> danhSachBan) {
        if (danhSachBan == null || danhSachBan.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn bàn");
        }

        Integer soKhach = datBan.getSoNguoiToiDa();

        int tongSucChua = danhSachBan.stream()
                .mapToInt(BanAn::getSucChuaToiDa)
                .sum();

        if (tongSucChua < soKhach) {
            throw new RuntimeException(
                    "Tổng sức chứa không đủ cho " + soKhach + " khách"
            );
        }

        Integer soGheDuToiDa = getInt(ThamSoHeThong.SO_GHE_DU_TOI_DA.getCode());

        if (tongSucChua > soKhach + soGheDuToiDa) {
            throw new RuntimeException(
                    "Bàn được chọn không phù hợp cho " + soKhach + " khách"
            );
        }

    }

    private com.fourspicehotpot.backend.modules.thamsohethong.entity.ThamSoHeThong getEntity(String code) {
        return thamSoHeThongRepository.findByMaThamSoAndTrangThaiTrue(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tham số: " + code));
    }

    @CacheEvict(value = "thamSo", key = "#code")
    public void clear(String code) {
    }


    @CacheEvict(value = "thamSo", allEntries = true)
    public void clearAll() {
    }

    private void validateRequest(ThamSoHeThongRequest request) {
        if (request.getTenThamSo() == null || request.getTenThamSo().isBlank()) {
            throw new RuntimeException("Tên tham số không được để trống");
        }

        if (request.getGiaTri() == null || request.getGiaTri().isBlank()) {
            throw new RuntimeException("Giá trị không được để trống");
        }

        if (request.getKieuDuLieu() == null || request.getKieuDuLieu().isBlank()) {
            throw new RuntimeException("Kiểu dữ liệu không được để trống");
        }

        // validate kiểu dữ liệu
        String type = request.getKieuDuLieu().toUpperCase();

        try {
            if (type.equals("INT")) {
                Integer.parseInt(request.getGiaTri());
            } else if (type.equals("DOUBLE")) {
                Double.parseDouble(request.getGiaTri());
            }
        } catch (Exception e) {
            throw new RuntimeException("Giá trị không đúng kiểu " + type);
        }
    }
}
