package com.spring.ai.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
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

    public void processDocument(MultipartFile file) throws IOException {

        // We will add the reader here
        Path tempFile = Files.createTempFile("upload-","-" + file.getOriginalFilename());
        try {

            Files.write( tempFile, file.getBytes());
            FileSystemResource resource =new FileSystemResource(tempFile);
            TikaDocumentReader reader =new TikaDocumentReader(resource);

            List<Document> documents =reader.get();

            Map<String, Object> metadata =new HashMap<>();
            metadata.put("fileName",file.getOriginalFilename());
            metadata.put("category","BANKING");
            metadata.put("documentType",determineDocumentType(file));
            knowledgeService.storeDocuments(documents,metadata);
        } finally {

            Files.deleteIfExists(tempFile);
        }
    }
    private String determineDocumentType(
            MultipartFile file) {

        String fileName =
                file.getOriginalFilename();

        if (fileName == null) {
            return "UNKNOWN";
        }

        if (fileName.endsWith(".pdf")) {
            return "PDF";
        }

        if (fileName.endsWith(".docx")) {
            return "DOCX";
        }

        if (fileName.endsWith(".html")) {
            return "HTML";
        }

        if (fileName.endsWith(".txt")) {
            return "TEXT";
        }

        return "UNKNOWN";
    }
}
