package com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response;

import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TinNhanResponse {
    private Long idTinNhan;
    private String noiDung;
    private String loaiTinNhan;
    private Boolean daDoc;
    private LocalDateTime thoiGian;
    private String loaiNguoiGui;
    private String tenKhachHang;
    private String tenNhanVien;
    private String duongDanFile;
    private List<SanPhamResponse> products;
}
