package com.fourspicehotpot.backend.modules.chatbot.service.serviceImpl;

import com.fourspicehotpot.backend.modules.chatbot.dto.response.ChatResponse;
import com.fourspicehotpot.backend.modules.chatbot.integration.ai.OpenAIClient;
import com.fourspicehotpot.backend.modules.chatbot.service.ChatService;
import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import com.fourspicehotpot.backend.modules.setlau.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final OpenAIClient openAIClient;
    private final SanPhamService sanPhamService;

    @Override
    public ChatResponse askAI(String message) {
        List<SanPhamResponse> products = new ArrayList<>();
        String context = "";

        String msgLower = message.toLowerCase();
        if (msgLower.contains("set lẩu") || msgLower.contains("ưu đãi")
                || msgLower.contains("khuyến mãi") || msgLower.contains("giảm giá")) {

            products = sanPhamService.getSanPhamDotGiamGia()
                    .stream()
                    .filter(sp -> Boolean.TRUE.equals(sp.getLaSet()))
                    .collect(Collectors.toList());

            StringBuilder sb = new StringBuilder("Danh sách set lẩu đang giảm giá:\n");
            for (SanPhamResponse sp : products) {
                sb.append("- ").append(sp.getTenSanPham())
                        .append(" | Giá gốc: ").append(sp.getGia()).append("đ")
                        .append(" | Giảm: ").append(sp.getPhanTramGiam()).append("%")
                        .append(" | Còn: ").append(sp.getGiaSauGiam()).append("đ\n");
            }
            context = sb.toString();
        }

        String answer = openAIClient.ask(message, context);
        return ChatResponse.builder()
                .answer(answer)
                .products(products)
                .build();
    }
}
