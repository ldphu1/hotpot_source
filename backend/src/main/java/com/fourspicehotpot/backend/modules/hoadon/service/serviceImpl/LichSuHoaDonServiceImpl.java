package com.fourspicehotpot.backend.modules.hoadon.service.serviceImpl;

import com.fourspicehotpot.backend.modules.hoadon.service.LichSuHoaDonService;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.hoadon.entity.LichSuHoaDon;
import com.fourspicehotpot.backend.modules.hoadon.repository.LichSuHoaDonRepository;
import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import com.fourspicehotpot.backend.modules.nhanvien.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Service
public class LichSuHoaDonServiceImpl implements LichSuHoaDonService {

    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;
    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Override
    public void saveLichSuHoaDon(HoaDon hoaDon, String hanhDong, Integer trangThai, String ghiChu) {

        ZoneId zoneVN = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime now = LocalDateTime.now(zoneVN);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String taiKhoan = "Hệ thống";
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            taiKhoan = auth.getName();
        }
        NhanVien nhanVien = nhanVienRepository.findNhanVienByTaiKhoan(taiKhoan)
                .orElse(null);
        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .hoaDon(hoaDon)
                .nhanVien(nhanVien)
                .ngayTao(LocalDateTime.now())
                .thoiGian(now)
                .hanhDong(hanhDong)
                .trangThai(trangThai)
                .ghiChu(ghiChu)
                .build();

        lichSuHoaDonRepository.save(lichSu);
    }
}
