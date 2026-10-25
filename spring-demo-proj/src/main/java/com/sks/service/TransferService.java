package com.sks.service;

import com.sks.dto.PaymentRequest;
import com.sks.exception.AccountNotFoundException;
import com.sks.exception.InsufficientBalanceException;
import com.sks.model.Account;
import com.sks.model.OutboxEvent;
import com.sks.model.Transfer;
import com.sks.model.TransferStatus;
import com.sks.repository.AccountRepository;
import com.sks.repository.OutboxRepository;
import com.sks.repository.TransferRepository;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final OutboxRepository outboxRepository;
    private final TransactionIdGenerator transactionIdGenerator;

    public TransferService(AccountRepository accountRepository, TransferRepository transferRepository, OutboxRepository outboxRepository, TransactionIdGenerator transactionIdGenerator) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
        this.outboxRepository = outboxRepository;
        this.transactionIdGenerator = transactionIdGenerator;
    }

    @Transactional
    public Transfer transfer(PaymentRequest paymentRequest) {

        if (paymentRequest.getAmount() == null || paymentRequest.getAmount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Withdrawal amount must be positive"
            );
        }

        Account from = accountRepository.findByAccountNumber(paymentRequest.getFromAccount()).orElseThrow(()->  new AccountNotFoundException("From Account doesnot exist or inactive"));
        Account to = accountRepository.findByAccountNumber(paymentRequest.getToAccount()).orElseThrow(()->  new AccountNotFoundException("To Account doesnot exist or inactive"));

        if (from.getBalance().compareTo(paymentRequest.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient account balance"
            );
        }
        from.setBalance(from.getBalance().subtract(paymentRequest.getAmount()));
        to.setBalance(to.getBalance().add(paymentRequest.getAmount()));

        accountRepository.save(from);
        accountRepository.save(to);

        //Write Business data and event in OutBox table
        Transfer transfer = new Transfer();
        transfer.setFromAccount(paymentRequest.getFromAccount());
        transfer.setToAccount(paymentRequest.getToAccount());
        transfer.setAmount(paymentRequest.getAmount());
        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setTransactionId(transactionIdGenerator.generateTransactionId());
        Transfer saveTransfer = transferRepository.save(transfer);
        //kafka will pick this event later from DB.
        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID().toString(),
                transfer.getTransactionId(),
                "TRANSFER_CREATED",
                createEventPayload(transfer)
        );

        outboxRepository.save(event);
        return saveTransfer;
    }

    private String createEventPayload(Transfer transfer) {
        // Serialize the transfer event to JSON.
        return """
               {
                 "transactionId": "%s",
                 "status": "%s"
               }
               """.formatted(
                transfer.getTransactionId(),
                transfer.getStatus()
        );
    }
    public Optional<Account> getAccountDetails(String accountNumber)throws Exception{
        return Optional.ofNullable(accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new AccountNotFoundException("Account Not Found")));
    }

}
