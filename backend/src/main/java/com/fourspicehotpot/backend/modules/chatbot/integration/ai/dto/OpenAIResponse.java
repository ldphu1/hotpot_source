package com.fourspicehotpot.backend.modules.chatbot.integration.ai.dto;

import lombok.Data;

import java.util.List;

@Data
public class OpenAIResponse {

    private List<ChoiceDTO> choices;

}