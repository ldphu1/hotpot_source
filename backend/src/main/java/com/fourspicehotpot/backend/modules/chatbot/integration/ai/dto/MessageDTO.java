package com.fourspicehotpot.backend.modules.chatbot.integration.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {

    private String role;

    private String content;

}