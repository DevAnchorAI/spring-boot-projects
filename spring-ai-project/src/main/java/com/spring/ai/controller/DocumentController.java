package com.spring.ai.controller;

import com.spring.ai.service.DocumentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
    @PostMapping("/upload")
    public String upload(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        documentService.processDocument(file);

        return "Document uploaded and indexed successfully";
    }
}
