package com.fintech.wallet.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter @Setter
public class TransferRequest {
    @NotNull
    @Size(min = 1)
    @Pattern(regexp = "^[0-9]{5,20}$", message = "Account number must be 5-20 digits")
    private String fromAccountNumber;

    @NotNull
    @Size(min = 1)
    @Pattern(regexp = "^[0-9]{5,20}$", message = "Account number must be 5-20 digits")
    private String toAccountNumber;

    @NotNull
    @Positive
    private BigDecimal amount;
}