package com.spring.ai.service;

import com.spring.ai.dto.BankingAssistantResponse;
import com.spring.ai.dto.DocumentQAResponse;
import com.spring.ai.guardrail.BankingOutputGuard;
import com.spring.ai.guardrail.PromptInjectionGuard;
import com.spring.ai.guardrail.PromptSafetyService;
import com.spring.ai.tools.BankingTools;
import com.spring.ai.tools.RagTools;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private final ChatClient chatClient;
    private final KnowledgeService knowledgeService;
    private final BankingTools bankingTools;
    private final RagTools ragTools;
    private final EmbeddingModel embeddingModel;
    private  final PromptInjectionGuard promptInjectionGuard;
    private final BankingOutputGuard bankingOutputGuard;
    private final PromptSafetyService promptSafetyService;

    public ChatbotService(
            ChatClient.Builder chatClientBuilder,
            KnowledgeService knowledgeService,
            RagTools ragTools,
            BankingTools bankingTools,
            ChatMemory chatMemory, EmbeddingModel embeddingModel, PromptInjectionGuard promptInjectionGuard, BankingOutputGuard bankingOutputGuard, PromptSafetyService promptSafetyService) {
        this.knowledgeService = knowledgeService;
        this.ragTools = ragTools;
        this.bankingTools = bankingTools;
        this.embeddingModel = embeddingModel;
        this.promptInjectionGuard = promptInjectionGuard;
        this.bankingOutputGuard = bankingOutputGuard;
        this.promptSafetyService = promptSafetyService;


        MessageChatMemoryAdvisor memoryAdvisor =
                MessageChatMemoryAdvisor.builder(chatMemory)
                        .build();

        this.chatClient = chatClientBuilder.defaultAdvisors(memoryAdvisor).build();

    }

    public DocumentQAResponse ask(String question,String documentType) {

        //1. Basic input validation / prompt injection protection.
        String validationResult = promptInjectionGuard.validate(question);

        if (validationResult != null) {
            return new DocumentQAResponse(validationResult, List.of());
        }
        //2. Optional LLM-based security classification.
        if (promptSafetyService.isSafePrompt(question)) {

            return new DocumentQAResponse("Your request cannot be processed.", List.of());
        }
        // 3. Search Vector DB
        //Retrieve relevant chunks
        List<Document> documents = knowledgeService.search(question, 4,documentType);

        // 4. Build context
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));


        // 5. Send context + question to LLM
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

       //6. Source of truth / citations.
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

    BeanOutputConverter<BankingAssistantResponse> converter =  new BeanOutputConverter<>(  BankingAssistantResponse.class);


    public BankingAssistantResponse chatConversation(
            String conversationId,
            String question) {
        System.out.println("conversationId: ["+conversationId+"]");

        //1. Validate conversation ID
        if (conversationId == null || conversationId.isBlank()) {

            return new BankingAssistantResponse(null, false, "conversationId cannot be empty.", null);
        }
        //2. first-level prompt injection protection
        String validationResult =promptInjectionGuard.validate(question);
        if (validationResult != null) {
            System.out.println(  "Prompt injection blocked by keyword guard.");
            return new BankingAssistantResponse(null, false, validationResult, null);
        }


       //3. Second-level security classification
        if (!promptSafetyService.isSafePrompt(question)) {
            System.out.println(     "Prompt injection blocked by AI security classifier.");
            return new BankingAssistantResponse(null, false, "Your request cannot be processed.", null);
        }

        //4. Explicit output converter
        BeanOutputConverter<BankingAssistantResponse> converter =new BeanOutputConverter<>(BankingAssistantResponse.class);

        //5. Main ChatClient
        BankingAssistantResponse response = chatClient
                    .prompt()
                    .system("""
                            You are an intelligent banking assistant.

                            You have access to banking tools.

                            Available banking operations:

                            1. TRANSACTION_STATUS
                               Use getTransactionStatus when the user asks
                               about a transaction.

                            2. ACCOUNT_BALANCE
                               Use getAccountBalance when the user asks
                               about an account balance.

                            3. RECENT_TRANSACTIONS
                               Use getRecentTransactions when the user asks
                               about recent transactions.

                            Always use the appropriate banking tool when
                            real-time banking information is required.

                            Never invent banking information.

                            After calling the tool, create the final response
                            using exactly these four fields:

                            intent:
                            The appropriate BankingIntent value.

                            success:
                            true when the requested banking information was
                            successfully retrieved from the tool.
                            false when the operation fails.

                            message:
                            A short human-readable explanation.

                            data:
                            The actual data returned by the banking tool.

                            IMPORTANT:
                            Never omit the success field.
                            Never set success to null.
                            """)
                    .user(user -> user
                            .text("""
                                    User question:

                                    {question}

                                    {format}
                                    """)
                            .param("question", question)
                            .param("format", converter.getFormat())
                    )
                    .tools(
                            ragTools,
                            bankingTools
                    )
                    //Chat Memory
                    .advisors(
                            advisor -> advisor.param(
                                    ChatMemory.CONVERSATION_ID,
                                    conversationId
                            )
                    )
                    .call()
                    .entity(converter);//to get Structured response

        //6. OUTPUT GUARDRAIL: validate AI output
        return bankingOutputGuard.validateResponse(response);

    }

    public float[] generateEmbedding(String text) {

        return embeddingModel
                .embed(text);
    }



}
