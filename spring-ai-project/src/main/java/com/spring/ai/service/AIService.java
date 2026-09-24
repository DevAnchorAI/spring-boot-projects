package com.spring.ai.service;

import com.spring.ai.test.EmbeddingService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    private final ChatClient chatClient;
    private final EmbeddingService embeddingService;
    public AIService(ChatClient.Builder builder, EmbeddingService embeddingService) {
        this.chatClient = builder.build();
        this.embeddingService = embeddingService;
    }

    public String ask(String question) {

        return chatClient
                .prompt()
                .system("""
                    You are a professional banking customer support assistant.

                    Answer clearly and concisely.
                    Never invent customer account information.
                    If you don't know the answer, say you don't know.
                    """)
                .user(question)
                .call()
                .content();
    }
}