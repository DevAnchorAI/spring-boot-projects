package com.spring.ai.controller;

//import com.spring.ai.test.ChatbotService;
import com.spring.ai.dto.DocumentQARequest;
import com.spring.ai.dto.DocumentQAResponse;
import com.spring.ai.service.ChatbotVectorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

//    private final ChatbotService chatbotService;
//    public ChatController(ChatbotService chatbotService) {
//        this.chatbotService = chatbotService;
//    }
    private final ChatbotVectorService chatbotVectorService;

    public ChatController(ChatbotVectorService chatbotVectorService) {
        this.chatbotVectorService = chatbotVectorService;
    }
    @GetMapping("/ask")
    public String ask(@RequestParam String question) {

        return chatbotVectorService.ask(question);
    }

    @PostMapping("/chat")
    public DocumentQAResponse chat(@RequestBody String message, @RequestParam(required = false)
    String documentType) {
        //return chatbotService.chat(message);
        return chatbotVectorService.chat(message,documentType);
    }

    @PostMapping("/askQuestion")
    public DocumentQAResponse askQuestion(@RequestBody  DocumentQARequest documentQARequest){
        return chatbotVectorService.chat(documentQARequest.question(),documentQARequest.documentType());
    }

    @PostMapping("/askTools")
    public String chatTools(@RequestBody String message) {
        return chatbotVectorService.chatTools(message);
    }

}