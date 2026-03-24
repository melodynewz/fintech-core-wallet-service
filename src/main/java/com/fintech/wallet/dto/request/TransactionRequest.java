package com.fintech.wallet.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter @Setter
public class TransactionRequest {
    @NotNull
    @Size(min = 1)
    @Pattern(regexp = "^[0-9]{5,20}$", message = "Account number must be 5-20 digits")
    private String accountNumber;

    @NotNull
    @Positive
    private BigDecimal amount;
}