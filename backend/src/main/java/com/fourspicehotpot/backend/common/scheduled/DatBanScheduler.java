package com.fourspicehotpot.backend.common.scheduled;

import com.fourspicehotpot.backend.modules.checkin.service.CheckInService;
import com.fourspicehotpot.backend.modules.datban.service.DatBanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class DatBanScheduler {
    @Autowired
    private DatBanService datBanService;

    @Scheduled(fixedRate = 5000)
    public void autoHuy() {
        datBanService.autoHuyDatBan();
    }

}
