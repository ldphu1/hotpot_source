package com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CuocTroChuyenResponse {
    Long id;
    Long idNhanVienXyLy;
    Long idKhachHang;
    String tenKhachHang;
    String tenNhanVienXyLy;
    String tenKhach;
    String loai;
    String trangThai;
    LocalDateTime thoiGianTao;
    LocalDateTime thoiGianCapNhat;
    LocalDateTime thoiGianDong;
    Long idTinNhanCuoi;
    String tinNhanCuoi;
    String loaiTinNhan;
    LocalDateTime thoiGianTinNhanCuoi;
    Integer soTinChuaDoc;

}
