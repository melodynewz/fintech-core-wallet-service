package com.fintech.wallet.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter @Setter
public class TransactionRequest {
    @NotNull
    @Size(min = 1)
    private String accountNumber;

    @NotNull
    @Positive
    private BigDecimal amount;
}