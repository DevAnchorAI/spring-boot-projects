package com.spring.ai.workflow;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import com.spring.ai.dto.BankingIntent;

@Service
public class IntentClassifier {

    private final ChatClient chatClient;

    public IntentClassifier(
            ChatClient.Builder chatClientBuilder) {

        this.chatClient =
                chatClientBuilder.build();
    }

    public BankingIntent classify(
            String question) {

        String result =
                chatClient
                        .prompt()
                        .system("""
                                You are a banking request classifier.

                                Classify the user request into exactly
                                one of these categories:

                                TRANSACTION_STATUS
                                ACCOUNT_BALANCE
                                RECENT_TRANSACTIONS
                                DOCUMENT_QA
                                GENERAL

                                Examples:

                                "What is the status of TXN1001?"
                                -> TRANSACTION_STATUS

                                "What is the balance of ACC1001?"
                                -> ACCOUNT_BALANCE

                                "Show my recent transactions."
                                -> RECENT_TRANSACTIONS

                                "What is the transaction limit?"
                                -> DOCUMENT_QA

                                "What documents are needed to open an account?"
                                -> DOCUMENT_QA

                                "Hello"
                                -> GENERAL

                                Return ONLY the category name.
                                """)
                        .user(question)
                        .call()
                        .content();

        return BankingIntent.valueOf(
                result.trim()
                        .toUpperCase()
        );
    }
}