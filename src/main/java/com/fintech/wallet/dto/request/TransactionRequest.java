package com.fintech.wallet.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class TransactionRequest {
    private String accountNumber;
    private BigDecimal amount;
}