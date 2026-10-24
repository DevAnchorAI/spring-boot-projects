package com.sks.service;

import com.sks.dto.PaymentRequest;
import com.sks.exception.AccountNotFoundException;
import com.sks.exception.InsufficientBalanceException;
import com.sks.model.Account;
import com.sks.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TransferService {

    private final AccountRepository accountRepository;

    public TransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void transfer(PaymentRequest paymentRequest) {

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
    }

    public Optional<Account> getAccountDetails(String accountNumber)throws Exception{
        return Optional.ofNullable(accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new AccountNotFoundException("Account Not Found")));
    }
}
