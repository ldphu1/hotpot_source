package com.fourspicehotpot.backend.modules.thucdon.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerMonDatTruocRequest {

    private Long idSanPham;
    private Integer soLuong;

}
