package com.fourspicehotpot.backend.modules.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KhuVucAppResponse {
    private Integer id;
    private String tenKhuVuc;
}
