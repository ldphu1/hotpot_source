package com.fourspicehotpot.backend.modules.khachhang.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerKhachHangResponse {
    private Long id;
    private String tenKhachHang;
    private String soDienThoai;
    private String email;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngaySinh;
    private Boolean gioiTinh;
    private CustomerDiaChiKhachHangResponse defaultAddress;
    private List<CustomerDiaChiKhachHangResponse> addresses;
}
