package com.fourspicehotpot.backend.modules.khachhang.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerKhachHangRequest {

    @NotBlank(message = "Vui lòng nhập họ tên")
    private String tenKhachHang;
    private String email;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(
            regexp = "^(03|05|07|08|09)[0-9]{8}$",
            message = "Số điện thoại không hợp lệ"
    )
    private String soDienThoai;
    private Boolean gioiTinh;

    @NotNull(message = "Vui lòng nhập ngày sinh")
    @PastOrPresent(message = "Ngày sinh không được lớn hơn ngày hiện tại")
    private LocalDate ngaySinh;

    @NotBlank(message = "Vui lòng nhập địa chỉ")
    private CustomerDiaChiRequest diaChi;
}
