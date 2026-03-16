package com.fintech.wallet.repository;

import com.fintech.wallet.model.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByAccountNumber(String accountNumber);
}
