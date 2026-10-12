package com.fourspicehotpot.backend.modules.checkin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchKhachChoCheckInRequest {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngayFrom;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngayTo;
}
