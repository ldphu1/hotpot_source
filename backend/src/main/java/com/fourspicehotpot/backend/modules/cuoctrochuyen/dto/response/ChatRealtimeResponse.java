package com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response;

import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRealtimeResponse {
    private Long idCuocTroChuyen;
    private Long idTinNhan;

    private String noiDung;

    private String loaiNguoiGui;

    private LocalDateTime thoiGian;

    private List<SanPhamResponse> products;
}