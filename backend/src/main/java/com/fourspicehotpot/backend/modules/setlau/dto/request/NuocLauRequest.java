package com.fourspicehotpot.backend.modules.setlau.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuocLauRequest {
    private String maNuocLau;

    @NotBlank(message = "Vui lòng nhập tên nước lẩu")
    private String tenNuocLau;

    @NotBlank(message = "Vui lòng nhập mô tả")
    private String moTa;

    @NotNull(message = "Vui lòng nhập giá phụ thu")
    private BigDecimal giaPhuThu;

    private String sortGia;
    private String keyword;

    private String hinhAnh;
    private Boolean trangThaiXoa;
}
