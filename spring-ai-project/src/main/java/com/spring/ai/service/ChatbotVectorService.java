package com.spring.ai.service;

import com.spring.ai.dto.DocumentQAResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
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

    public DocumentQAResponse chat(String question,String documentType) {

        // 1. Search Vector DB
        //Retrieve relevant chunks
        List<Document> documents =
                knowledgeService.search(question, 4,documentType);

        // 2. Build context
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));


        // 3. Send context + question to LLM
       String answer =  chatClient
                .prompt()
                .system("""
                        You are a banking AI assistant.

                        Answer the question using ONLY the
                        information provided in the context.

                        If the answer is not available in the
                        context, say:
                        "I don't have enough information."

                        Do not make up information.

                        Context:
                        """ + context)
                .user(question)
                .call()
                .content();

       //now add source of truth in response
        List<String> sources = documents.stream()
                .map(document ->
                        String.valueOf(
                                document.getMetadata()
                                        .get("fileName")
                        )
                )
                .distinct()
                .toList();

        return new DocumentQAResponse(
                answer,
                sources
        );
    }
}