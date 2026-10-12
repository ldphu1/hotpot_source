package com.fourspicehotpot.backend.modules.thongke.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingChannelResponse {

    private Integer online;
    private Integer taiQuay;

}