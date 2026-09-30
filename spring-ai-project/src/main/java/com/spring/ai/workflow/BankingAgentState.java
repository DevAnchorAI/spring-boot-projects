package com.spring.ai.workflow;

import com.spring.ai.dto.BankingIntent;
import org.apache.poi.ss.formula.functions.T;

public class BankingAgentState {

    private String conversationId;

    private String question;

    private BankingIntent intent;

    private String transactionId;

    private String accountNumber;

    private String retrievedContext;

    private Object toolResult;

    private String response;

    private boolean success;

    public BankingAgentState() {
    }

    public BankingAgentState(
            String conversationId,
            String question) {

        this.conversationId = conversationId;
        this.question = question;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public BankingIntent getIntent() {
        return intent;
    }

    public void setIntent(BankingIntent intent) {
        this.intent = intent;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getRetrievedContext() {
        return retrievedContext;
    }

    public void setRetrievedContext(String retrievedContext) {
        this.retrievedContext = retrievedContext;
    }

    public Object getToolResult() {
        return toolResult;
    }

    public void setToolResult(Object toolResult) {
        this.toolResult = toolResult;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
