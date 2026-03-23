package com.fintech.wallet.dto.response;

import com.fintech.wallet.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private Long id;
    private Long walletId;
    private String accountNumber;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDateTime transactionDate;
}