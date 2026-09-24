package com.spring.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatbotVectorService {

    private final ChatClient chatClient;
    private final KnowledgeService knowledgeService;

    public ChatbotVectorService(
            ChatClient.Builder builder,
            KnowledgeService knowledgeService) {

        this.chatClient = builder.build();
        this.knowledgeService = knowledgeService;
    }

    public String chat(String question) {

        // 1. Search Vector DB
        List<Document> documents =
                knowledgeService.search(question, 3);

        // 2. Build context
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        // 3. Send context + question to LLM
        return chatClient
                .prompt()
                .system("""
                        You are a banking AI assistant.

                        Answer the question using ONLY the
                        information provided in the context.

                        If the answer is not available in the
                        context, say:
                        "I don't have enough information."

                        Context:
                        """ + context)
                .user(question)
                .call()
                .content();
    }
}