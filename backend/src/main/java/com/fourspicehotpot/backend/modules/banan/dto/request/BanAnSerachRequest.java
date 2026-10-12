package com.fourspicehotpot.backend.modules.banan.dto.request;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class BanAnSerachRequest {
    private String tang;
    private Integer soCho;
    private Integer trangThai;
}
