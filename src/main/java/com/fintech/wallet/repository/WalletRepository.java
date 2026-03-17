package com.fintech.wallet.repository;

import com.fintech.wallet.model.entity.Wallet;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByAccountNumber(String accountNumber);

    // แบบล็อค (ใช้ตอนจะ ฝาก/ถอน/โอน)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.accountNumber = :accountNumber")
    Optional<Wallet> findByAccountNumberForUpdate(String accountNumber);
}