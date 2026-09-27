package com.spring.ai.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class KnowledgeService {

    private final VectorStore vectorStore;

    public KnowledgeService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void addDocument(String text, Map<String, Object> metadata) {

        Document document = new Document(text, metadata);
        vectorStore.add(List.of(document));
    }

    public void storeDocuments(List<Document> documents, Map<String, Object> metadata) {

        // Add metadata
        documents.forEach(document -> {
            document.getMetadata().putAll(metadata);
        });
        System.out.println("metadata:"+metadata);

        // Split documents into chunks
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(350)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(10000)
                .build();
        List<Document> chunks = splitter.apply(documents);

        // Store chunks + embeddings + metadata in Vector DB
        vectorStore.add(chunks);
    }


    public List<Document> search(String query, int topK) {

        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .build()
        );
    }
}
