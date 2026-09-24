package com.spring.ai.controller;

import com.spring.ai.service.DocumentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public String addDocument(@RequestBody String text) {

        documentService.addBankingPolicy(text);

        return "Document stored successfully";
    }
}
