package com.fourspicehotpot.backend.modules.khachhang.service;

import com.fourspicehotpot.backend.modules.khachhang.dto.request.KhachHangRequest;
import com.fourspicehotpot.backend.modules.khachhang.dto.response.KhachHangResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KhachHangService {

    List<KhachHangResponse> getAllKhachHang();

    KhachHangResponse create(KhachHangRequest req, MultipartFile file);

    void doiTrangThai(Long id);

    void update(Long id, KhachHangRequest request, MultipartFile file);

    KhachHangResponse getById(Long id);

    boolean existsEmail(String email);

    boolean existsPhone(String phone);


    List<KhachHangResponse> search(String keyword);
}
