package com.spring.ai.service;

import com.spring.ai.dto.DocumentQAResponse;
import com.spring.ai.tools.BankingTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatbotVectorService {

    private final ChatClient chatClient;
    private final KnowledgeService knowledgeService;
    private final BankingTools bankingTools;
    public ChatbotVectorService(
            ChatClient.Builder builder,
            KnowledgeService knowledgeService, BankingTools bankingTools) {

        this.chatClient = builder.build();
        this.knowledgeService = knowledgeService;
        this.bankingTools = bankingTools;
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

    public String chatTools(String question) {

        return chatClient
                .prompt()
                .user(question)
                .tools(bankingTools)
                .call()
                .content();
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