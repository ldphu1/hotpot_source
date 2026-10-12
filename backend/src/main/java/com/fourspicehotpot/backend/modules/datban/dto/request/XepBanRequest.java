package com.fourspicehotpot.backend.modules.datban.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class XepBanRequest {
    private Long idDatBan;
    private List<Integer> listIdBan;
}
