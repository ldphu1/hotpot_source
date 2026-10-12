package com.fourspicehotpot.backend.common.scheduled;

import com.fourspicehotpot.backend.modules.checkin.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CheckInScheduler {

    @Autowired
    private CheckInService checkInService;

    @Scheduled(fixedRate = 60000) // Chạy mỗi phút
    public void autoDonBan() {
        checkInService.autoDonBan();
    }
}
