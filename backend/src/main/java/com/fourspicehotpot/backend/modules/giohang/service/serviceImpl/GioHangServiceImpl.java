package com.fourspicehotpot.backend.modules.giohang.service.serviceImpl;

import com.fourspicehotpot.backend.modules.giohang.dto.request.GioHangRequest;
import com.fourspicehotpot.backend.modules.giohang.dto.response.GioHangResponse;
import com.fourspicehotpot.backend.modules.giohang.entity.GioHang;
import com.fourspicehotpot.backend.modules.giohang.entity.GioHangChiTiet;
import com.fourspicehotpot.backend.modules.giohang.mapper.GioHangMapper;
import com.fourspicehotpot.backend.modules.giohang.repository.GioHangChiTietRepository;
import com.fourspicehotpot.backend.modules.giohang.repository.GioHangRepository;
import com.fourspicehotpot.backend.modules.giohang.service.GioHangService;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.khachhang.repository.KhachHangRepository;
import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import com.fourspicehotpot.backend.modules.setlau.repository.SanPhamRepository;
import com.fourspicehotpot.backend.modules.thamsohethong.service.ThamSoHeThongService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GioHangServiceImpl implements GioHangService {

    private final KhachHangRepository khachHangRepository;
    private final SanPhamRepository sanPhamRepository;
    private final GioHangRepository gioHangRepository;
    private final GioHangMapper gioHangMapper;
    private final GioHangChiTietRepository gioHangChiTietRepository;
    private final ThamSoHeThongService thamSoHeThongService;
    @Transactional
    @Override
    public GioHangResponse themVaoGioHang(GioHangRequest request, HttpServletRequest httpRequest) {

        SanPham sp = sanPhamRepository.findById(request.getIdSanPham())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        GioHang gioHang;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean checkLogin = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName());
        if (checkLogin) {
            String username = auth.getName();

            KhachHang kh = khachHangRepository.findByEmail(username);
            gioHang = gioHangRepository.findByKhachHang(kh)
                    .orElseGet(() -> {
                        GioHang gh = new GioHang();
                        gh.setKhachHang(kh);
                        gh.setThoiGianTao(LocalDateTime.now());
                        gh.setTrangThai(1);
                        return gioHangRepository.save(gh);
                    });

        } else {
            String sessionId = httpRequest.getSession().getId();
            gioHang = gioHangRepository.findBySessionId(sessionId)
                    .orElseGet(() -> {
                        GioHang gh = new GioHang();
                        gh.setSessionId(sessionId);
                        gh.setThoiGianTao(LocalDateTime.now());
                        gh.setTrangThai(1);
                        return gioHangRepository.save(gh);
                    });
        }

        if (gioHang.getChiTietList() == null) {
            gioHang.setChiTietList(new ArrayList<>());
        }

        int MAX_LAU = Integer.parseInt(thamSoHeThongService.get("MAX_LAU"));
        int MAX_DO_AN_KEM = Integer.parseInt(thamSoHeThongService.get("MAX_DO_AN_KEM"));
        int MAX_DO_UONG = Integer.parseInt(thamSoHeThongService.get("MAX_DO_UONG"));
        int MAX_DO_TRANG_MIENG = Integer.parseInt(thamSoHeThongService.get("MAX_DO_TRANG_MIENG"));
        int MAX_DO_NHUNG_LAU = Integer.parseInt(thamSoHeThongService.get("MAX_DO_NHUNG_LAU"));

        String danhMuc = sp.getDanhMuc().getTenDanhMuc();
        Long idGioHang = gioHang.getId();
        Long idDanhMuc = Long.valueOf(sp.getDanhMuc().getId());
        int soLuongThem = request.getSoLuong();
        int tongHienTai = gioHangChiTietRepository.sumSoLuongByDanhMucId(idGioHang, idDanhMuc);


        if ("Lẩu".equalsIgnoreCase(danhMuc)) {
            if (tongHienTai + soLuongThem > MAX_LAU) {
                throw new RuntimeException("Chỉ được thêm tối đa 3 lẩu");
            }
        } else if ("Đồ nhúng lẩu".equalsIgnoreCase(danhMuc)) {
            if (tongHienTai + soLuongThem > MAX_DO_NHUNG_LAU) {
                throw new RuntimeException("Chỉ được thêm tối đa 10 đồ nhúng");
            }
        } else if ("Đồ ăn kèm".equalsIgnoreCase(danhMuc)) {
            if (tongHienTai + soLuongThem > MAX_DO_AN_KEM) {
                throw new RuntimeException("Chỉ được thêm tối đa 10 đồ ăn kèm");
            }
        } else if ("Đồ uống".equalsIgnoreCase(danhMuc)) {
            if (tongHienTai + soLuongThem > MAX_DO_UONG) {
                throw new RuntimeException("Chỉ được thêm tối đa 20 đồ uống");
            }
        } else if ("Đồ tráng miệng".equalsIgnoreCase(danhMuc)) {
            if (tongHienTai + soLuongThem > MAX_DO_TRANG_MIENG) {
                throw new RuntimeException("Chỉ được thêm tối đa 20 đồ tráng miệng");
            }
        }
        // check đã có món chưa
        Optional<GioHangChiTiet> existing =
                gioHangChiTietRepository.findByGioHangAndSanPham(gioHang, sp);
        if (existing.isPresent()) {

            GioHangChiTiet ct = existing.get();
            ct.setSoLuong(ct.getSoLuong() + soLuongThem);

            if (!gioHang.getChiTietList().contains(ct)) {
                gioHang.getChiTietList().add(ct);
            }

        } else {
            GioHangChiTiet ct = new GioHangChiTiet();
            ct.setGioHang(gioHang);
            ct.setSanPham(sp);
            ct.setSoLuong(request.getSoLuong());
            ct.setGia(sp.getGia());

            gioHangChiTietRepository.save(ct);
            gioHang.getChiTietList().add(ct);
        }

        gioHangRepository.save(gioHang);
        return gioHangMapper.mapToResponse(gioHang);
    }
}
