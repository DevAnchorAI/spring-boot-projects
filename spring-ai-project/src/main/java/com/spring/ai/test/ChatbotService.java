package com.spring.ai.test;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ChatbotService {

    private final ChatClient chatClient;
    private final EmbeddingService embeddingService;

    private final List<String> conversationHistory =
            new ArrayList<>();

    public ChatbotService(ChatClient.Builder builder, EmbeddingService embeddingService) {
        this.chatClient = builder.build();
        this.embeddingService = embeddingService;
    }


//    public String chat(String message) {
//
//        conversationHistory.add("User: " + message);
//
//        String conversation =
//                String.join("\n", conversationHistory);
//
//        String response = chatClient
//                .prompt()
//                .system("""
//                    You are a helpful AI assistant.
//                    Answer based on the conversation history.
//                    """)
//                .user(conversation)
//                .call()
//                .content();
//
//        conversationHistory.add("Assistant: " + response);
//
//        return response;
//    }

    public String chat(String message) {

        // 1. Generate embedding for user message
        float[] embedding =
                embeddingService.generateEmbedding(message);

        System.out.println("Embedding generated."+ Arrays.toString(embedding));
        System.out.println("Vector size: " + embedding.length);

        // 2. Store conversation
        conversationHistory.add("User: " + message);

        String conversation =
                String.join("\n", conversationHistory);

        // 3. Ask LLM
        String response = chatClient
                .prompt()
                .system("""
                    You are a helpful AI assistant.
                    Answer based on the conversation history.
                    """)
                .user(conversation)
                .call()
                .content();

        // 4. Store AI response
        conversationHistory.add(
                "Assistant: " + response
        );

        return response;
    }

}
