package com.fourspicehotpot.backend.modules.setlau.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamRequest {

    @NotBlank(message = "Chưa nhập tên sản phẩm")
    private String tenSanPham;

    @NotNull(message = "Chưa chọn danh mục")
    private Integer idDanhMuc;

    @NotNull(message = "Chưa nhập giá")
    @Positive(message = "Đơn giá không hợp lệ")
    private BigDecimal gia;

    @NotNull(message = "Chưa chọn định lượng")
    private Integer idDinhLuong;
    private String moTa;


}
