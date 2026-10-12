package com.fourspicehotpot.backend.modules.dinhluong.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DonViRequest {
    private String tenDonVi;
    private List<BigDecimal> dinhLuongs;
    private List<Integer> idDanhMucs;
}
