package com.fourspicehotpot.backend.modules.hoadon.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuHoaDonResponse {

    private Long id;

    private Long idHoaDon;

    private LocalDateTime ngayTao;

    private LocalDateTime ngayCapNhat;

    private LocalDateTime thoiGian;

    private String hanhDong;

    private Integer trangThai;

    private String ghiChu;
    private String tenNhanVien;

}