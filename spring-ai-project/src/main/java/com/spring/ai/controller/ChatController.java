package com.spring.ai.controller;

//import com.spring.ai.test.ChatbotService;
import com.spring.ai.dto.DocumentQARequest;
import com.spring.ai.dto.DocumentQAResponse;
import com.spring.ai.service.ChatbotVectorService;
import org.springframework.web.bind.annotation.*;

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
    public DocumentQAResponse chat(@RequestBody String message, @RequestParam(required = false)
    String documentType) {
        //return chatbotService.chat(message);
        return chatbotVectorService.chat(message,documentType);
    }

    @PostMapping("/askQuestion")
    public DocumentQAResponse askQuestion(@RequestBody  DocumentQARequest documentQARequest){
        return chatbotVectorService.chat(documentQARequest.question(),documentQARequest.documentType());
    }


}