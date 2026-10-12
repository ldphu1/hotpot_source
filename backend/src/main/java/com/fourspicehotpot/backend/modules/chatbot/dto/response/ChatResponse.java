package com.fourspicehotpot.backend.modules.chatbot.dto.response;

import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ChatResponse {

    private String answer;

    private List<SanPhamResponse> products;

}