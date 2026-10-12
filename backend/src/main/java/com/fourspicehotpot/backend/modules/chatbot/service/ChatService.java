package com.fourspicehotpot.backend.modules.chatbot.service;

import com.fourspicehotpot.backend.modules.chatbot.dto.response.ChatResponse;

public interface ChatService {

    ChatResponse askAI(String message);

}