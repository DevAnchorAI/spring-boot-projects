package com.sks.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PaymentRequest {

    @NotNull(message = "Source account is required")
    private String fromAccount;

    @NotNull(message = "Destination account is required")
    private String toAccount;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    public @NotNull(message = "Source account is required") String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(@NotNull(message = "Source account is required") String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public @NotNull(message = "Destination account is required") String getToAccount() {
        return toAccount;
    }

    public void setToAccount(@NotNull(message = "Destination account is required") String toAccount) {
        this.toAccount = toAccount;
    }

    public @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(@NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") BigDecimal amount) {
        this.amount = amount;
    }
}