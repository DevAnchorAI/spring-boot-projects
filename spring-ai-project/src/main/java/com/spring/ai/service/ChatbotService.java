package com.spring.ai.service;

import com.spring.ai.dto.DocumentQAResponse;
import com.spring.ai.tools.BankingTools;
import com.spring.ai.tools.RagTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private final ChatClient chatClient;
    private final KnowledgeService knowledgeService;
    private final BankingTools bankingTools;
    private final RagTools ragTools;
    private final EmbeddingModel embeddingModel;


    public ChatbotService(
            ChatClient.Builder chatClientBuilder,
            KnowledgeService knowledgeService,
            RagTools ragTools,
            BankingTools bankingTools,
            ChatMemory chatMemory, EmbeddingModel embeddingModel) {
        this.knowledgeService = knowledgeService;
        this.ragTools = ragTools;
        this.bankingTools = bankingTools;
        this.embeddingModel = embeddingModel;


        MessageChatMemoryAdvisor memoryAdvisor =
                MessageChatMemoryAdvisor.builder(chatMemory)
                        .build();

        this.chatClient =
                chatClientBuilder
                        .defaultAdvisors(memoryAdvisor)
                        .build();
    }


    public DocumentQAResponse chat(String question,String documentType) {
        // 1. Generate embedding for user message
//        float[] embedding = generateEmbedding(question);
//        System.out.println("Embedding generated."+ Arrays.toString(embedding));
//        System.out.println("Vector size: " + embedding.length);

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

    public String chatConversation(
            String conversationId,
            String question) {
        System.out.println("conversationId>>"+conversationId);
        return chatClient
                .prompt()
                .system("""
                        You are an intelligent banking assistant.

                        You have access to two types of capabilities.

                        1. Knowledge Base:
                           Use searchDocuments when the user asks
                           about banking policies, loan policies,
                           account opening, FAQs, transaction limits,
                           or other information contained in documents.

                        2. Banking Tools:
                           Use banking tools when the user asks for
                           real-time transaction or account information.

                        Use conversation history when the user's
                        current question refers to something discussed
                        earlier.

                        Never invent banking information.

                        If information is available through a tool,
                        use the appropriate tool instead of guessing.
                        """)
                .user(question)
                .tools(
                        ragTools,
                        bankingTools
                )
                //now add the advisor to keep the conversation history
                .advisors(
                        advisor -> advisor.param(
                                ChatMemory.CONVERSATION_ID,
                                conversationId
                        )
                )
                .call()
                .content();
    }

    public float[] generateEmbedding(String text) {

        return embeddingModel
                .embed(text);
    }
}
