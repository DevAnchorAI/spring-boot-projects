package com.spring.ai.service;

import com.spring.ai.dto.DocumentQAResponse;
import com.spring.ai.tools.BankingTools;
import com.spring.ai.tools.RagTools;
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
    private final RagTools ragTools;
    public ChatbotVectorService(
            ChatClient.Builder builder,
            KnowledgeService knowledgeService, BankingTools bankingTools, RagTools ragTools) {

        this.chatClient = builder.build();
        this.knowledgeService = knowledgeService;
        this.bankingTools = bankingTools;
        this.ragTools = ragTools;
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

    public String askBankingTools(String question) {

        return chatClient
                .prompt()
                .system("""
                        You are a banking assistant.

                        You can use banking tools when
                        real-time banking information is required.

                        Never invent transaction status,
                        account balance, or transaction details.

                        Use the appropriate tool when
                        the user asks for real-time banking data.
                        """)
                .user(question)
                .tools(bankingTools)
                .call()
                .content();
    }

    public String askRagAndBankingTools(String question) {

        return chatClient
                .prompt()
                .system("""
                        You are an intelligent banking assistant.

                        You have access to two types of capabilities:

                        1. Knowledge Base:
                           Use searchDocuments when the user asks
                           about banking policies, loan policies,
                           account opening, FAQs, transaction limits,
                           or other information contained in documents.

                        2. Banking Tools:
                           Use banking tools when the user asks for
                           real-time transaction or account information.

                        Never invent banking information.

                        If the information is available through a
                        tool, use the tool instead of guessing.

                        If the user asks a question requiring both
                        policy information and real-time information,
                        use both capabilities.

                        Give a concise and accurate final answer.
                        """)
                .user(question)
                .tools(
                        ragTools,
                        bankingTools
                )
                .call()
                .content();
    }
}