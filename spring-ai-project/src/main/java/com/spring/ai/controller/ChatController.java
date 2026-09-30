package com.spring.ai.controller;

import com.spring.ai.dto.*;
import com.spring.ai.service.ChatbotService;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatbotService chatbotService;
    public ChatController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/ask")
    public DocumentQAResponse chat(@RequestBody String message, @RequestParam(required = false)
    String documentType) {
        return chatbotService.ask(message,documentType);
    }

    @PostMapping("/chat")
    public BankingAssistantResponse chatConversation(
            @RequestBody ChatRequest request) {

        return chatbotService.chatConversation(request.conversationId(), request.question());
    }

    @PostMapping("/chatAgent")
    public BankingAssistantResponse chatAgent(
            @RequestBody ChatRequest request) {

        return chatbotService.chatAgent(request.conversationId(), request.question());
    }

}