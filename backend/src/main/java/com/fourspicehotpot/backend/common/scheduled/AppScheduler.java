package com.fourspicehotpot.backend.common.scheduled;

import com.fourspicehotpot.backend.integration.websocket.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AppScheduler {

    @Autowired
    private WebSocketService webSocketService;

    // Chạy mỗi 10 giây để tự động gửi thông báo cập nhật giá trị (như đợt giảm giá hết hạn, bàn ăn...)
    @Scheduled(fixedRate = 10000)
    public void autoReloadData() {
        webSocketService.sendRealtimeChonBan();
        webSocketService.sendTopic("/topic/thucdon", "RELOAD");
        webSocketService.sendTopic("/topic/discount", "RELOAD");
    }
}
