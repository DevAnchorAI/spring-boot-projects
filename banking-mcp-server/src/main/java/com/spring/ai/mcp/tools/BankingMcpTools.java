package com.spring.ai.mcp.tools;

import com.spring.ai.mcp.dto.TransactionStatusResponse;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class BankingMcpTools {

    @McpTool(
            name = "getTransactionStatus",
            description = """
                    Retrieves the current status of a banking transaction.
                    Use this tool when the user asks for the status of
                    a specific transaction.
                    """
    )
    public TransactionStatusResponse getTransactionStatus(

            @McpToolParam(
                    description = "Transaction ID such as TXN1001",
                    required = true
            )
            String transactionId) {

        System.out.println(
                "MCP TOOL CALLED: getTransactionStatus("
                        + transactionId + ")"
        );

        // Temporary mock implementation
        return new TransactionStatusResponse(
                transactionId,
                "COMPLETED",
                25000.00
        );
    }
}