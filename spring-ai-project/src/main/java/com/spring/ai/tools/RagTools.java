package com.spring.ai.tools;

import com.spring.ai.service.KnowledgeService;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RagTools {

    private final KnowledgeService knowledgeService;

    public RagTools(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @Tool(
            description = """
        Search the banking knowledge base for policy, product,
        account opening, loan, transaction, FAQ, and other
        document-related information.
        Use this tool when the user asks about information
        contained in company documents or banking policies.
        """
    )
    public String searchDocuments(String question) {

        System.out.println(
                "Tool called: searchDocuments()"
        );

        List<Document> documents =
                knowledgeService.search(
                        question,
                        4,
                        null
                );

        if (documents == null || documents.isEmpty()) {
            return "No relevant information was found in the knowledge base.";
        }

        return documents.stream()
                .map(document -> {

                    String fileName =
                            String.valueOf(
                                    document.getMetadata()
                                            .get("fileName")
                            );

                    return """
                            Source: %s
                            Content:
                            %s
                            """.formatted(
                            fileName,
                            document.getText()
                    );
                })
                .collect(Collectors.joining("\n\n"));
    }
}
