package com.spring.ai.tools;
import com.spring.ai.dto.AccountBalanceData;
import com.spring.ai.dto.RecentTransactionData;
import com.spring.ai.dto.Transaction;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BankingTools {

    @Tool( description = "Get the current status and details of a banking transaction.")
    public Transaction getTransactionStatus(String transactionId) {
        System.out.println("Tool called: getTransactionStatus()");

        if ("TXN1001".equals(transactionId)) {
            return new Transaction(transactionId,"COMPLETED",new BigDecimal("25000"),"DOMESTIC_TRANSFER");
        }

        if ("TXN1002".equals(transactionId)) {
            return new Transaction(transactionId,"PENDING",new BigDecimal("50000"),"DOMESTIC_TRANSFER");
        }

        if ("TXN1003".equals(transactionId)) {
            return new Transaction(transactionId,"FAILED",new BigDecimal("15000"), "BILL_PAYMENT");
        }

        throw new IllegalArgumentException(
                "Transaction not found: " + transactionId
        );
    }


    @Tool(description = "Get the available balance and status of a bank account .")
    public AccountBalanceData getAccountBalance(String accountNumber) {

        System.out.println("Tool called: getAccountBalance()");

        if ("ACC1001".equalsIgnoreCase(accountNumber)) {

            return new AccountBalanceData(accountNumber,new BigDecimal("125500"),"INR","ACTIVE");
        }

        if ("ACC1002".equalsIgnoreCase(accountNumber)) {

            return new AccountBalanceData( accountNumber,new BigDecimal("75,250"),"INR","ACTIVE");
        }
        return new AccountBalanceData(accountNumber,new BigDecimal("0,0"),"INR","INACTIVE");
    }


    @Tool(description = "Get the recent transactions for a bank account.")
    public List<RecentTransactionData> getRecentTransactions(String accountNumber) {

        System.out.println( "Tool called: getRecentTransactions()");

        return List.of(
                new RecentTransactionData(
                        "TXN1001",
                        "DOMESTIC_TRANSFER",
                        new BigDecimal("25000"),
                        "COMPLETED"
                ),
                new RecentTransactionData(
                        "TXN1002",
                        "DOMESTIC_TRANSFER",
                        new BigDecimal("50000"),
                        "PENDING"
                ), new RecentTransactionData(
                        "TXN1003",
                        "BILL_PAYMENT",
                        new BigDecimal("15000"),
                        "FAILED"
                )
        );
    }

}