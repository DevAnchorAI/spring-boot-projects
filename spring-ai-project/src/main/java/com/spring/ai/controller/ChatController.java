package com.spring.ai.controller;

import com.spring.ai.dto.ChatRequest;
import com.spring.ai.dto.DocumentQARequest;
import com.spring.ai.dto.DocumentQAResponse;
import com.spring.ai.service.ChatbotService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatbotService chatbotService;
    public ChatController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/chat")
    public DocumentQAResponse chat(@RequestBody String message, @RequestParam(required = false)
    String documentType) {
        return chatbotService.chat(message,documentType);
    }

    @PostMapping("/askBankingTools")
    public String askTools(@RequestBody String message) {
        return chatbotService.askBankingTools(message);
    }

    @PostMapping("/askRagAndBankingTools")
    public String askRagAndBankingTools(@RequestBody String message) {
        return chatbotService.askRagAndBankingTools(message);
    }

    @PostMapping("/chatConversation")
    public String chatConversation(
            @RequestBody ChatRequest request) {

        return chatbotService.chatConversation(request.conversationId(), request.question());
    }

}