package com.spring.ai.tools;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class BankingTools {

    @Tool(
            description = "Get the current status of a banking transaction using its transaction ID"
    )
    public String getTransactionStatus(String transactionId) {

        System.out.println(
                "Tool called: getTransactionStatus()"
        );

        if ("TXN1001".equalsIgnoreCase(transactionId)) {

            return """
                    Transaction ID: TXN1001
                    Status: COMPLETED
                    Amount: INR 25,000
                    Transaction Type: DOMESTIC_TRANSFER
                    """;
        }

        if ("TXN1002".equalsIgnoreCase(transactionId)) {

            return """
                    Transaction ID: TXN1002
                    Status: PENDING
                    Amount: INR 50,000
                    Transaction Type: DOMESTIC_TRANSFER
                    """;
        }

        if ("TXN1003".equalsIgnoreCase(transactionId)) {

            return """
                    Transaction ID: TXN1003
                    Status: FAILED
                    Amount: INR 15,000
                    Transaction Type: BILL_PAYMENT
                    """;
        }

        return "Transaction not found for transaction ID: "
                + transactionId;
    }


    @Tool(
            description = "Get the current available account balance using the bank account number"
    )
    public String getAccountBalance(String accountNumber) {

        System.out.println(
                "Tool called: getAccountBalance()"
        );

        if ("ACC1001".equalsIgnoreCase(accountNumber)) {

            return """
                    Account Number: ACC1001
                    Available Balance: INR 125,500
                    Currency: INR
                    Account Status: ACTIVE
                    """;
        }

        if ("ACC1002".equalsIgnoreCase(accountNumber)) {

            return """
                    Account Number: ACC1002
                    Available Balance: INR 75,250
                    Currency: INR
                    Account Status: ACTIVE
                    """;
        }

        return "Account not found for account number: "
                + accountNumber;
    }


    @Tool(
            description = "Get recent banking transactions for a customer account using the account number"
    )
    public String getRecentTransactions(
            String accountNumber) {

        System.out.println(
                "Tool called: getRecentTransactions()"
        );

        if ("ACC1001".equalsIgnoreCase(accountNumber)) {

            return """
                    Recent transactions for account ACC1001:

                    1. TXN1001
                       Type: DOMESTIC_TRANSFER
                       Amount: INR 25,000
                       Status: COMPLETED

                    2. TXN1002
                       Type: DOMESTIC_TRANSFER
                       Amount: INR 50,000
                       Status: PENDING

                    3. TXN1003
                       Type: BILL_PAYMENT
                       Amount: INR 15,000
                       Status: FAILED
                    """;
        }

        if ("ACC1002".equalsIgnoreCase(accountNumber)) {

            return """
                    Recent transactions for account ACC1002:

                    1. TXN2001
                       Type: UTILITY_PAYMENT
                       Amount: INR 5,000
                       Status: COMPLETED

                    2. TXN2002
                       Type: DOMESTIC_TRANSFER
                       Amount: INR 10,000
                       Status: COMPLETED
                    """;
        }

        return "No account found for account number: "
                + accountNumber;
    }
}