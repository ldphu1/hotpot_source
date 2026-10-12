package com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response;

import java.time.LocalDateTime;

public interface CuocTroChuyenProjection {
    Long getId();

    Long getIdKhachHang();

    String getTenKhachHang();

    Long getIdNhanVienXuLy();

    String getTenNhanVienXuLy();

    String getTenKhach();

    String getLoai();

    String getTrangThai();

    LocalDateTime getThoiGianTao();

    LocalDateTime getThoiGianCapNhat();

    LocalDateTime getThoiGianDong();

    Long getIdTinNhanCuoi();

    String getTinNhanCuoi();

    String getLoaiTinNhan();

    LocalDateTime getThoiGianTinNhanCuoi();

    Integer getSoTinChuaDoc();
}
