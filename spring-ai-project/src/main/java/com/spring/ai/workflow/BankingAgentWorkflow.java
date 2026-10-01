package com.spring.ai.workflow;

import com.spring.ai.dto.BankingAssistantResponse;
import com.spring.ai.dto.BankingIntent;
import com.spring.ai.service.KnowledgeService;
import com.spring.ai.tools.BankingTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;
@Service
public class BankingAgentWorkflow {

    private final IntentClassifier intentClassifier;
    private final BankingTools bankingTools;
    private final KnowledgeService knowledgeService;
    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpTools;
    public BankingAgentWorkflow(
            IntentClassifier intentClassifier, BankingTools bankingTools, KnowledgeService knowledgeService, ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpTools) {

        this.intentClassifier =intentClassifier;
        this.bankingTools = bankingTools;
        this.knowledgeService = knowledgeService;
        this.chatClient = chatClientBuilder.build();
        this.mcpTools = mcpTools;
    }

    public BankingAssistantResponse execute(
            BankingAgentState state) {

        // 1. Analyze request
        analyzeIntent(state);

        // 2. Route request to appropriate intent
        route(state);

        // 3. Generate final response
        generateResponse(state);

        return new BankingAssistantResponse(
                state.getIntent(),
                state.isSuccess(),
                state.getResponse(),
                state.getToolResult()
        );
    }

    private void analyzeIntent(
            BankingAgentState state) {

        // We'll implement this using ChatClient.
        BankingIntent intent =intentClassifier.classify(state.getQuestion());
        state.setIntent(intent);

        System.out.println( "Detected intent = " + intent);

    }

    private void route(
            BankingAgentState state) {

        switch (state.getIntent().name()) {

            case "TRANSACTION_STATUS" ->
                    handleTransactionStatus(state);

            case "ACCOUNT_BALANCE" ->
                    handleAccountBalance(state);

            case "RECENT_TRANSACTIONS" ->
                    handleRecentTransactions(state);

            case "DOCUMENT_QA" ->
                    handleDocumentQA(state);

            default ->
                    handleGeneralQuestion(state);
        }
    }

    private void handleTransactionStatus(
            BankingAgentState state) {

        // Banking tool
       String transactionId = extractTransactionId( state.getQuestion());
        state.setTransactionId(transactionId);

        var result =bankingTools.getTransactionStatus(transactionId);

        state.setToolResult(result);

        state.setSuccess(true);
    }

    private void handleAccountBalance(
            BankingAgentState state) {
        // Banking tool
        String accountNumber =
                extractAccountNumber(
                        state.getQuestion()
                );

        state.setAccountNumber(
                accountNumber
        );

        var result =
                bankingTools.getAccountBalance(
                        accountNumber
                );

        state.setToolResult(result);

        state.setSuccess(true);
    }

    private void handleRecentTransactions(
            BankingAgentState state) {

        // Banking tool
        String accountNumber =
                extractAccountNumber(
                        state.getQuestion()
                );

        state.setAccountNumber(
                accountNumber
        );

        var result =
                bankingTools.getRecentTransactions(
                        accountNumber
                );

        state.setToolResult(result);

        state.setSuccess(true);
    }

    private void handleDocumentQA(
            BankingAgentState state) {

        // RAG
        var documents =
                knowledgeService.search(
                        state.getQuestion(),
                        4,
                        null
                );

        String context =
                documents.stream()
                        .map(Document::getText)
                        .collect(
                                java.util.stream.Collectors.joining(
                                        "\n\n"
                                )
                        );

        state.setRetrievedContext(context);

        state.setSuccess(true);
    }

    private void handleGeneralQuestion(
            BankingAgentState state) {

        // Normal ChatClient
    }

    private void generateResponse(
            BankingAgentState state) {

        // Final LLM response
        String prompt = """
            You are a banking assistant.

            Generate a concise answer to the user's question.

            User question:
            %s

            Intent:
            %s

            Tool result:
            %s

            Retrieved context:
            %s

            Never invent information.
            Use only the provided result/context.
            """.formatted(
                state.getQuestion(),
                state.getIntent(),
                state.getToolResult(),
                state.getRetrievedContext()
        );

        String response =
                chatClient
                        .prompt()
                        .user(prompt)
                        .tools(mcpTools)
                        .call()
                        .content();

        state.setResponse(response);
    }


    private String extractTransactionId(
            String question) {

        var matcher =
                java.util.regex.Pattern
                        .compile("TXN\\d+",
                                java.util.regex.Pattern.CASE_INSENSITIVE)
                        .matcher(question);

        if (matcher.find()) {
            return matcher.group()
                    .toUpperCase();
        }

        throw new IllegalArgumentException(
                "Transaction ID not found"
        );
    }

    private String extractAccountNumber(
            String question) {

        var matcher =
                java.util.regex.Pattern
                        .compile("ACC\\d+",
                                java.util.regex.Pattern.CASE_INSENSITIVE)
                        .matcher(question);

        if (matcher.find()) {
            return matcher.group()
                    .toUpperCase();
        }

        throw new IllegalArgumentException(
                "Transaction ID not found"
        );
    }
}