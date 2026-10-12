package com.fourspicehotpot.backend.modules.nhanvien.service;

import com.fourspicehotpot.backend.modules.nhanvien.dto.request.NhanVienRequest;
import com.fourspicehotpot.backend.modules.nhanvien.dto.response.NhanVienResponse;
import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface NhanVienService {
    List<NhanVienResponse> findAll();

    NhanVien add(NhanVienRequest dto, MultipartFile anh) throws IOException;

    NhanVienResponse getById(Long id);

    ;

    NhanVien update(Long id, NhanVienRequest dto, MultipartFile anhNhanVien) throws IOException;

    void doiTrangThai(Long id);

    List<NhanVienResponse> search(
            String ten,
            String contact,
            Boolean trangThai
    );

    boolean existsByEmail(String email);



    boolean existsBySoDienThoai(String phone);
}
