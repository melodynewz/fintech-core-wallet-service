package com.fintech.wallet.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
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
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Wallet deposit (String accountNumber, BigDecimal amount){
        Wallet wallet = walletRepository.findByAccountNumberForUpdate(accountNumber).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

        // 1. อัปเดตยอดเงิน
        wallet.setBalance(wallet.getBalance().add(amount));
        
        // 2. บันทึกประวัติ
        saveTransaction(wallet, amount, TransactionType.DEPOSIT);
        
        return walletRepository.save(wallet);
    }

    // (ถอนเงิน + Lock + เช็คยอด)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
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
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Wallet transfer(String fromAccount, String toAccount, BigDecimal amount) {
        // เรียงลำดับการล็อคตามเลขบัญชีเพื่อป้องกัน Deadlock
        List<Wallet> lockedWallets = lockWalletsInOrder(fromAccount, toAccount);
        Wallet sender = lockedWallets.get(0).getAccountNumber().equals(fromAccount) ?
                       lockedWallets.get(0) : lockedWallets.get(1);
        Wallet receiver = lockedWallets.get(0).getAccountNumber().equals(toAccount) ?
                        lockedWallets.get(0) : lockedWallets.get(1);
        
        // ตรวจสอบว่าเงินพอไหม
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("ยอดเงินไม่เพียงพอ");
        }
        
        // หักเงินจาก sender
        sender.setBalance(sender.getBalance().subtract(amount));
        // เพิ่มเงินให้ receiver
        receiver.setBalance(receiver.getBalance().add(amount));
        
        // บันทึกประวัติการทำรายการ
        saveTransaction(sender, amount, TransactionType.WITHDRAW);
        saveTransaction(receiver, amount, TransactionType.DEPOSIT);
        
        // บันทึกการเปลี่ยนแปลงทั้งสองบัญชี
        walletRepository.saveAll(List.of(sender, receiver));
        return sender;
    }
    
    private List<Wallet> lockWalletsInOrder(String account1, String account2) {
        // เรียงลำดับ account numbers เพื่อป้องกัน deadlock
        String first, second;
        if (account1.compareTo(account2) <= 0) {
            first = account1;
            second = account2;
        } else {
            first = account2;
            second = account1;
        }
        
        // lock บัญชีแรก
        Wallet wallet1 = walletRepository.findByAccountNumberForUpdate(first)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + first));
        
        // lock บัญชีที่สอง (อาจจะเป็นบัญชีเดียวกันถ้า account1 เท่ากับ account2)
        Wallet wallet2;
        if (first.equals(second)) {
            wallet2 = wallet1;
        } else {
            wallet2 = walletRepository.findByAccountNumberForUpdate(second)
                    .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + second));
        }
        
        // คืนลิสต์ wallet ตามลำดับที่ lock (ไม่จำเป็นต้องเรียงตาม input)
        return List.of(wallet1, wallet2);
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
