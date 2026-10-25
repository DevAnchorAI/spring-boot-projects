package com.sks.controller;
import com.sks.dto.ErrorResponse;
import com.sks.dto.PaymentRequest;
import com.sks.dto.ResponseDto;
import com.sks.exception.AccountNotFoundException;
import com.sks.exception.InsufficientBalanceException;
import com.sks.model.Account;
import com.sks.model.Transfer;
import com.sks.service.TransferService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    @Autowired
    TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<Transfer> transfer(
            @Valid @RequestBody PaymentRequest request) {
        Transfer transfer =transferService.transfer(request);
        return ResponseEntity.ok(transfer);
    }

    @GetMapping("/getAccount/{accountNumber}")
    public ResponseEntity<Optional<Account>> greetingMessag(@PathVariable String accountNumber)throws Exception{
        Optional<Account> account =transferService.getAccountDetails(accountNumber);

        return ResponseEntity.ok(account);
    }

}
