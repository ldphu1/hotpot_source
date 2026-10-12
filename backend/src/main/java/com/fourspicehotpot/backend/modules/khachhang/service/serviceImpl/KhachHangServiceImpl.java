package com.fourspicehotpot.backend.modules.khachhang.service.serviceImpl;

import com.fourspicehotpot.backend.modules.khachhang.service.KhachHangService;
import com.fourspicehotpot.backend.modules.khachhang.dto.request.DiaChiKhachHangRequest;
import com.fourspicehotpot.backend.modules.khachhang.dto.request.KhachHangRequest;
import com.fourspicehotpot.backend.modules.khachhang.dto.response.DiaChiKhachHangResponse;
import com.fourspicehotpot.backend.modules.khachhang.dto.response.KhachHangResponse;
import com.fourspicehotpot.backend.modules.khachhang.entity.DiaChiKhachHang;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.khachhang.repository.DiaChiRepository;
import com.fourspicehotpot.backend.modules.khachhang.repository.KhachHangRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class KhachHangServiceImpl implements KhachHangService {
    @Autowired
    KhachHangRepository khachHangRepository;
    @Autowired
    DiaChiRepository diaChiRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<KhachHangResponse> getAllKhachHang() {
        List<KhachHang> list = khachHangRepository.findAll();

        return list.stream().map(kh -> {
            KhachHangResponse dto = new KhachHangResponse();
            dto.setId(kh.getId());
            dto.setMaKhachHang(kh.getMaKhachHang());
            dto.setTenKhachHang(kh.getTenKhachHang());
            dto.setSoDienThoai(kh.getSoDienThoai());
            dto.setEmail(kh.getEmail());
            dto.setTrangThai(kh.getTrangThai());
            dto.setNgaySinh(kh.getNgaySinh());
            dto.setGioiTinh(kh.getGioiTinh());
            dto.setAnhDaiDien(
                    kh.getAnhDaiDien() != null
                            ? "/uploads/" + kh.getAnhDaiDien()
                            : null
            );

            List<DiaChiKhachHangResponse> diaChiList =
                    kh.getDiaChiList().stream().map(dc -> {
                        DiaChiKhachHangResponse d = new DiaChiKhachHangResponse();
                        d.setId(dc.getId());   // THÊM DÒNG NÀY
                        d.setTinhThanh(dc.getTinhThanh());
                        d.setXaPhuong(dc.getXaPhuong());
                        d.setDiaChiChiTiet(dc.getDiaChiChiTiet());
                        d.setMacDinh(dc.getMacDinh());
                        return d;
                    }).toList();

            dto.setDiaChiList(diaChiList);
            return dto;
        }).toList();
    }

    @Transactional
    public KhachHangResponse create(KhachHangRequest req, MultipartFile file) {
        // ===== CHECK TRÙNG =====
        if (khachHangRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        if (khachHangRepository.existsBySoDienThoai(req.getSoDienThoai())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        }
        KhachHang kh = new KhachHang();
        kh.setTenKhachHang(req.getHoTen());
        kh.setNgaySinh(req.getNgaySinh());
        kh.setGioiTinh(req.getGioiTinh());
        kh.setSoDienThoai(req.getSoDienThoai());
        kh.setEmail(req.getEmail());
        kh.setMatKhau(passwordEncoder.encode(req.getMatKhau()));
        /* ===== XỬ LÝ ẢNH ĐẠI DIỆN (ĐẶT NGAY Ở ĐÂY) ===== */
        try {
            if (file != null && !file.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get("uploads/" + fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());

                kh.setAnhDaiDien(fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu ảnh đại diện", e);
        }


        kh = khachHangRepository.save(kh);

        List<DiaChiKhachHangResponse> diaChiResponses = new ArrayList<>();

        for (DiaChiKhachHangRequest d : req.getDiaChiList()) {
            if (Boolean.TRUE.equals(d.getMacDinh())) {
                diaChiRepository.clearMacDinhByKhachHang(kh.getId());
            }
            DiaChiKhachHang dc = new DiaChiKhachHang();
            dc.setKhachHang(kh);
            dc.setTinhThanh(d.getTinhThanh());
            dc.setXaPhuong(d.getXaPhuong());
            dc.setDiaChiChiTiet(d.getDiaChiChiTiet());
            dc.setMacDinh(Boolean.TRUE.equals(d.getMacDinh()));

            diaChiRepository.save(dc);

            diaChiResponses.add(toDiaChiResponse(dc));
        }

        return toKhachHangResponse(kh, diaChiResponses);
    }

    private DiaChiKhachHangResponse toDiaChiResponse(DiaChiKhachHang dc) {
        DiaChiKhachHangResponse r = new DiaChiKhachHangResponse();
        r.setId(dc.getId());
        r.setTinhThanh(dc.getTinhThanh());
        r.setXaPhuong(dc.getXaPhuong());
        r.setDiaChiChiTiet(dc.getDiaChiChiTiet());
        r.setMacDinh(dc.getMacDinh());
        return r;
    }

    private KhachHangResponse toKhachHangResponse(
            KhachHang kh,
            List<DiaChiKhachHangResponse> diaChiList
    ) {
        KhachHangResponse r = new KhachHangResponse();
        r.setId(kh.getId());
        r.setTenKhachHang(kh.getTenKhachHang());
        if (kh.getNgaySinh() != null) {
            r.setNgaySinh(kh.getNgaySinh());
        } // Date
        r.setGioiTinh(kh.getGioiTinh());
        r.setSoDienThoai(kh.getSoDienThoai());
        r.setEmail(kh.getEmail());
        r.setAnhDaiDien(
                kh.getAnhDaiDien() != null
                        ? "/uploads/" + kh.getAnhDaiDien()
                        : null
        );

        r.setDiaChiList(diaChiList);
        return r;
    }

    @Transactional
    public void doiTrangThai(Long id) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng"));

        kh.setTrangThai(kh.getTrangThai() == 1 ? 0 : 1);
    }

    @Override
    @Transactional
    public void update(Long id, KhachHangRequest request, MultipartFile file) {

        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
// ===== CHECK TRÙNG EMAIL =====
        if (!kh.getEmail().equals(request.getEmail())
                && khachHangRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

// ===== CHECK TRÙNG PHONE =====
        if (!kh.getSoDienThoai().equals(request.getSoDienThoai())
                && khachHangRepository.existsBySoDienThoai(request.getSoDienThoai())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        }
        // ===== field thường =====
        kh.setTenKhachHang(request.getHoTen());
        kh.setNgaySinh(request.getNgaySinh());
        kh.setGioiTinh(request.getGioiTinh());
        kh.setSoDienThoai(request.getSoDienThoai());
        kh.setEmail(request.getEmail());

        // ===== ảnh =====
        if (file != null && !file.isEmpty()) {
            String fileName = saveFile(file); // mày tự implement
            kh.setAnhDaiDien(fileName);
        }


        // Xóa toàn bộ địa chỉ cũ
        diaChiRepository.deleteByKhachHangId(kh.getId());
        kh.getDiaChiList().clear();


        if (request.getDiaChiList() != null) {
            boolean foundMacDinh = false;

            for (DiaChiKhachHangRequest d : request.getDiaChiList()) {
                DiaChiKhachHang dc = new DiaChiKhachHang();

                if (d.getId() != null) {
                    dc = diaChiRepository.findById(d.getId()).orElse(new DiaChiKhachHang());
                } else {
                    dc = new DiaChiKhachHang();
                }

                dc.setTinhThanh(d.getTinhThanh());
                dc.setXaPhuong(d.getXaPhuong());
                dc.setDiaChiChiTiet(d.getDiaChiChiTiet());

                if (Boolean.TRUE.equals(d.getMacDinh()) && !foundMacDinh) {
                    dc.setMacDinh(true);
                    foundMacDinh = true;
                } else {
                    dc.setMacDinh(false);
                }

                dc.setKhachHang(kh);
                diaChiRepository.save(dc);
            }

        }

        khachHangRepository.save(kh);
    }

    private String saveFile(MultipartFile file) {
        // demo nhanh
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get("uploads", fileName);

        try {
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Upload ảnh thất bại");
        }

        return fileName;
    }

    @Override
    public KhachHangResponse getById(Long id) {

        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        KhachHangResponse res = new KhachHangResponse();

        // ===== FIELD CƠ BẢN =====
        res.setId(kh.getId());
        res.setMaKhachHang(kh.getMaKhachHang());
        res.setTenKhachHang(kh.getTenKhachHang());
        res.setSoDienThoai(kh.getSoDienThoai());
        res.setEmail(kh.getEmail());
        res.setNgaySinh(kh.getNgaySinh());
        res.setGioiTinh(kh.getGioiTinh());
        res.setTrangThai(kh.getTrangThai());
        res.setAnhDaiDien(kh.getAnhDaiDien());

        // ===== ĐỊA CHỈ =====
        List<DiaChiKhachHangResponse> diaChiList = kh.getDiaChiList()
                .stream()
                .map(dc -> {
                    DiaChiKhachHangResponse d = new DiaChiKhachHangResponse();
                    d.setId(dc.getId());
                    d.setTinhThanh(dc.getTinhThanh());
                    d.setXaPhuong(dc.getXaPhuong());
                    d.setDiaChiChiTiet(dc.getDiaChiChiTiet());
                    d.setMacDinh(dc.getMacDinh());
                    return d;
                })
                .toList();

        res.setDiaChiList(diaChiList);

        return res;
    }

    @Override
    public boolean existsEmail(String email) {
        return khachHangRepository.existsByEmail(email);
    }

    @Override
    public boolean existsPhone(String phone) {
        return khachHangRepository.existsBySoDienThoai(phone);
    }

    public List<KhachHangResponse> search(String keyword) {
        keyword = keyword.trim();

        List<KhachHang> list;

        if (keyword.matches("\\d+")) {
            list = khachHangRepository
                    .findByTenKhachHangContainingIgnoreCaseOrSoDienThoaiContaining(keyword, keyword);
        } else {
            list = khachHangRepository
                    .findByTenKhachHangContainingIgnoreCase(keyword);
        }

        return list.stream().map(kh -> new KhachHangResponse(
                kh.getId(),
                kh.getTenKhachHang(),
                kh.getSoDienThoai(),
                kh.getEmail(),
                kh.getNgaySinh()
        )).toList();
    }

}
