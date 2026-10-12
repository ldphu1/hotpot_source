package com.fourspicehotpot.backend.modules.thucdon.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DanhMucRequest {
    List<Long> idDonVi;
    private Integer id;
    @NotBlank(message = "Tên danh mục không được để trống")
    private String tenDanhMuc;
}
