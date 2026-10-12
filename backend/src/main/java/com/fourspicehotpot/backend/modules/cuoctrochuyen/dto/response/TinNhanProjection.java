package com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response;

import java.time.LocalDateTime;


public interface TinNhanProjection {
    Long getIdTinNhan();

    String getNoiDung();

    String getLoaiTinNhan();

    Boolean getDaDoc();

    LocalDateTime getThoiGian();

    String getLoaiNguoiGui();

    String getTenKhachHang();

    String getTenNhanVien();

    String getDuongDanFile();

   
}
