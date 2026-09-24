package com.spring.ai.service;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DocumentService {

    private final KnowledgeService knowledgeService;

    public DocumentService(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    public void addBankingPolicy(String text) {

        Map<String, Object> metadata = new HashMap<>();

        metadata.put("documentType", "TRANSACTION_POLICY");
        metadata.put("category", "BANKING");

        knowledgeService.addDocument(text, metadata);
    }
}
