package com.fourspicehotpot.backend.modules.setlau.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamListRequest {
    private List<SanPhamRequest> requests;
}
