package com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.request;

import lombok.Data;

@Data
public class ReplyTinNhanRequest {
    private Long idCuocTroChuyen;
    private String noiDung;
}