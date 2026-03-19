package com.fintech.wallet.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter @Builder
public class WalletResponse {
    private String accountNumber;
    private String ownerName;
    private BigDecimal balance;
}