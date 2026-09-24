package com.spring.ai.controller;

//import com.spring.ai.test.ChatbotService;
import com.spring.ai.service.ChatbotVectorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

//    private final ChatbotService chatbotService;
//    public ChatController(ChatbotService chatbotService) {
//        this.chatbotService = chatbotService;
//    }
    private final ChatbotVectorService chatbotVectorService;

    public ChatController(ChatbotVectorService chatbotVectorService) {
        this.chatbotVectorService = chatbotVectorService;
    }

    @PostMapping
    public String chat(@RequestBody String message) {
        //return chatbotService.chat(message);
        return chatbotVectorService.chat(message);
    }

}