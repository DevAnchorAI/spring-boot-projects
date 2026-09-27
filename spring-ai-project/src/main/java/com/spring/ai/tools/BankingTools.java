package com.spring.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class BankingTools {

    @Tool(description = "Get the current status of a banking transaction using its transaction ID")
    public String getTransactionStatus(String transactionId) {

        // For now, mock data.
        // Later replace this with a database/API call.

        if ("TXN1001".equals(transactionId)) {
            return """
                    Transaction ID: TXN1001
                    Status: COMPLETED
                    Amount: INR 25,000
                    """;
        }

        if ("TXN1002".equals(transactionId)) {
            return """
                    Transaction ID: TXN1002
                    Status: PENDING
                    Amount: INR 50,000
                    """;
        }

        return "Transaction not found: " + transactionId;
    }
}