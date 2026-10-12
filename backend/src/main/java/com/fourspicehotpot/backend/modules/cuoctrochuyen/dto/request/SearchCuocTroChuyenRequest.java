package com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchCuocTroChuyenRequest {
    String tenKhachHang;
    LocalDateTime cursor;

}
