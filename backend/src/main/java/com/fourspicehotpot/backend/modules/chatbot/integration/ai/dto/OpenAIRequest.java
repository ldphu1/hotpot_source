package com.fourspicehotpot.backend.modules.chatbot.integration.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIRequest {

    private String model;

    private List<MessageDTO> messages;

}