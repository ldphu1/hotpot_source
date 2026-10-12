package com.fourspicehotpot.backend.modules.dotgiamgia.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DotGiamGiaRequest {

    @NotBlank(message = "Tên đợt không được để trống")
    private String tenDot;


    @NotNull(message = "Giá trị giảm không được null")
    @DecimalMin(value = "0", message = "Giá trị giảm phải >= 0")
    private BigDecimal giaTriGiam;

    @NotNull(message = "Ngày bắt đầu không được null")
    private LocalDateTime ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được null")
    private LocalDateTime ngayKetThuc;

    @NotEmpty(message = "Phải chọn ít nhất 1 sản phẩm")
    private List<Long> sanPhamIds;
}
