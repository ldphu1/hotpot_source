package com.fourspicehotpot.backend.modules.datban.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fourspicehotpot.backend.modules.thucdon.dto.request.CustomerMonDatTruocRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CustomerDatBanRequest {

    // Lãnh Minh Hiếu
    List<CustomerMonDatTruocRequest> monDatTruoc;
    @NotBlank(message = "Tên khách không được để trống")
    private String tenKhachHang;
    @NotBlank(message = "Số điện thoại không được để trống")
    private String soDienThoai;
    @NotBlank(message = "Email không được để trống")
    private String email;
    /// ///////////////////////////////
    private Integer id;

    private Integer idBanAn;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "Thời gian đặt không được để trống")
    private LocalDateTime thoiGianDat;
    @NotNull(message = "Số người tối đa không được để trống")
    private Integer soNguoiToiDa;
    private String ghiChu;

    private String trangThai;
}