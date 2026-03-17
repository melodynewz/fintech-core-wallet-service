package com.fintech.wallet.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fintech.wallet.exception.InsufficientBalanceException;
import com.fintech.wallet.model.entity.Transaction;
import com.fintech.wallet.model.entity.Wallet;
import com.fintech.wallet.model.enums.TransactionType;
import com.fintech.wallet.repository.TransactionRepository;
import com.fintech.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletService {
    
    private WalletRepository walletRepository;
    private TransactionRepository transactionRepository;
    
    // Transactional ป้องกันกรณี เอาเงินใส่แล้วแต่ยังไม่ได้บันทึก transaction จะเกิดการ rollback
    // (ฝากเงิน + Lock)
    @Transactional
    public Wallet deposit (String accountNumber, BigDecimal amount){
        Wallet wallet = walletRepository.findByAccountNumberForUpdate(accountNumber).orElseThrow(() -> new RuntimeException("Wallet not found"));

        // 1. อัปเดตยอดเงิน
        wallet.setBalance(wallet.getBalance().add(amount));
        
        // 2. บันทึกประวัติ
        saveTransaction(wallet, amount, TransactionType.DEPOSIT);
        
        return walletRepository.save(wallet);
    }

    // (ถอนเงิน + Lock + เช็คยอด)
    @Transactional
    public Wallet withdraw(String accountNumber, BigDecimal amount) {
        Wallet wallet = walletRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // 1. ตรวจสอบว่าเงินพอไหม
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("ยอดเงินไม่เพียงพอ");
        }

        // 2. หักเงิน
        wallet.setBalance(wallet.getBalance().subtract(amount));

        // 3. บันทึกประวัติ
        saveTransaction(wallet, amount, TransactionType.WITHDRAW);

        return walletRepository.save(wallet);
    }

    // (โอนเงิน + Transactional)
    @Transactional
    public void transfer(String fromAccount, String toAccount, BigDecimal amount) {
        // ล็อคทั้งสองบัญชีเพื่อป้องกันปัญหา Race Condition
        // เรียงลำดับการล็อค (เช่น ตามเลขบัญชี) เพื่อป้องกัน Deadlock
        withdraw(fromAccount, amount);
        deposit(toAccount, amount);
    }

    public Wallet getWalletDetails(String accountNumber) {
        return walletRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    private void saveTransaction(Wallet wallet, BigDecimal amount, TransactionType type) {
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(type)
                .build();
        transactionRepository.save(transaction);
    }
}
