package com.fintech.wallet.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fintech.wallet.dto.response.TransactionResponse;
import com.fintech.wallet.exception.InsufficientBalanceException;
import com.fintech.wallet.exception.WalletNotFoundException;
import com.fintech.wallet.model.entity.Transaction;
import com.fintech.wallet.model.entity.Wallet;
import com.fintech.wallet.model.enums.TransactionType;
import com.fintech.wallet.repository.TransactionRepository;
import com.fintech.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletService {
   
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    
    // Transactional ป้องกันกรณี เอาเงินใส่แล้วแต่ยังไม่ได้บันทึก transaction จะเกิดการ rollback
    // (ฝากเงิน + Lock)
    @Transactional
    public Wallet deposit (String accountNumber, BigDecimal amount){
        Wallet wallet = walletRepository.findByAccountNumberForUpdate(accountNumber).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

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
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

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
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }

    public List<TransactionResponse> getTransactionHistory(String accountNumber) {
        Wallet wallet = walletRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
        List<Transaction> transactions = transactionRepository.findByWalletIdOrderByTransactionDateDesc(wallet.getId());
        return transactions.stream()
                .map(t -> TransactionResponse.builder()
                        .id(t.getId())
                        .walletId(t.getWallet().getId())
                        .accountNumber(t.getWallet().getAccountNumber())
                        .amount(t.getAmount())
                        .type(t.getType())
                        .transactionDate(t.getTransactionDate())
                        .build())
                .toList();
    }
    
    public Page<TransactionResponse> getTransactionHistory(String accountNumber, Pageable pageable) {
        Wallet wallet = walletRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
        Page<Transaction> transactions = transactionRepository.findByWalletId(wallet.getId(), pageable);
        return transactions.map(t -> TransactionResponse.builder()
                .id(t.getId())
                .walletId(t.getWallet().getId())
                .accountNumber(t.getWallet().getAccountNumber())
                .amount(t.getAmount())
                .type(t.getType())
                .transactionDate(t.getTransactionDate())
                .build());
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
