package com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GuiTinNhanRequest {
    String sessionId;
    String noiDung;
    String loaiTinNhan;
}