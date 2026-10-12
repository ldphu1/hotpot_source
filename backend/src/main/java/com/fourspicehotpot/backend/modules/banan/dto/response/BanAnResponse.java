package com.fourspicehotpot.backend.modules.banan.dto.response;


import com.fourspicehotpot.backend.modules.datban.dto.response.DatBanResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BanAnResponse {

    private Long id;

    private String maBanAn;

    private KhuVucResponse khuVuc;

    private String tenBanAn;

    private Integer sucChuaToiDa;

    private Integer trangThai;

    private LocalDateTime ngayTao;

    private LocalDateTime ngayCapNhat;

    private DatBanResponse datBan;

    private boolean coTheDat;


}
