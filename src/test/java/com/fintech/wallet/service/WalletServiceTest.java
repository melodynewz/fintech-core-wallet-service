package com.fintech.wallet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fintech.wallet.exception.InsufficientBalanceException;
import com.fintech.wallet.model.entity.Transaction;
import com.fintech.wallet.model.entity.Wallet;
import com.fintech.wallet.repository.TransactionRepository;
import com.fintech.wallet.repository.WalletRepository;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {
    
    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;

    private Wallet mockWallet;

    @BeforeEach
    void setUp() {
        mockWallet = Wallet.builder()
                .accountNumber("12345")
                .balance(new BigDecimal("1000.00"))
                .build();
    }

    // --- TEST DEPOSIT ---
    @Test
    void deposit_Success() {
        // Given
        when(walletRepository.findByAccountNumberForUpdate("12345")).thenReturn(Optional.of(mockWallet));
        when(walletRepository.save(any())).thenReturn(mockWallet);

        // When
        Wallet result = walletService.deposit("12345", new BigDecimal("500.00"));

        // Then
        assertEquals(new BigDecimal("1500.00"), result.getBalance());
        verify(transactionRepository, times(1)).save(any()); // ต้องมีการบันทึกประวัติ 1 ครั้ง
    }

    // --- TEST WITHDRAW ---
    @Test
    void withdraw_Success() {
        // Given
        when(walletRepository.findByAccountNumberForUpdate("12345")).thenReturn(Optional.of(mockWallet));
        when(walletRepository.save(any())).thenReturn(mockWallet);

        // When
        Wallet result = walletService.withdraw("12345", new BigDecimal("400.00"));

        // Then
        assertEquals(new BigDecimal("600.00"), result.getBalance());
    }

    @Test
    @DisplayName("ถอนเงินเกินบัญชี - ต้องพ่น Exception และไม่บันทึกอะไรเลย")
    void withdraw_InsufficientBalance_ShouldThrowException() {
        // Given
        Wallet wallet = Wallet.builder().accountNumber("A").balance(new BigDecimal("100")).build();
        when(walletRepository.findByAccountNumberForUpdate("A")).thenReturn(Optional.of(wallet));

        // When & Then
        assertThrows(InsufficientBalanceException.class, () -> {
            walletService.withdraw("A", new BigDecimal("500"));
        });

        // ตรวจสอบว่าไม่มีการบันทึก transaction ใดๆ เพราะ error ก่อน
        verify(transactionRepository, never()).save(any());
    }

    // --- TEST TRANSFER ---
    @Test
    void transfer_Success() {
        // Given
        Wallet receiverWallet = Wallet.builder()
                .accountNumber("67890")
                .balance(new BigDecimal("100.00"))
                .build();

        when(walletRepository.findByAccountNumberForUpdate("12345")).thenReturn(Optional.of(mockWallet));
        when(walletRepository.findByAccountNumberForUpdate("67890")).thenReturn(Optional.of(receiverWallet));

        // When
        Wallet returnedSender = walletService.transfer("12345", "67890", new BigDecimal("200.00"));

        // Then
        assertEquals(new BigDecimal("800.00"), mockWallet.getBalance());
        assertEquals(new BigDecimal("300.00"), receiverWallet.getBalance());
        assertEquals(mockWallet, returnedSender); // ตรวจสอบว่าคืน sender เดียวกัน
        verify(transactionRepository, times(2)).save(any()); // ต้องมีประวัติ 2 รายการ (ถอนและฝาก)
    }

    @Test
    @DisplayName("โอนเงินสำเร็จ - ยอดเงินต้องตัด และต้องบันทึกประวัติ 2 รายการ")
    void transfer_Success_ShouldUpdateBalancesAndSaveHistory() {
        // [Given] เตรียมข้อมูลจำลอง
        Wallet sender = Wallet.builder().accountNumber("A").balance(new BigDecimal("1000")).build();
        Wallet receiver = Wallet.builder().accountNumber("B").balance(new BigDecimal("500")).build();
        BigDecimal amount = new BigDecimal("200");

        // กำหนดลำดับการ lock ตาม account number (A < B)
        when(walletRepository.findByAccountNumberForUpdate("A")).thenReturn(Optional.of(sender));
        when(walletRepository.findByAccountNumberForUpdate("B")).thenReturn(Optional.of(receiver));

        // [When] รันคำสั่งโอนเงิน
        Wallet returnedSender = walletService.transfer("A", "B", amount);

        // [Then] ตรวจสอบผลลัพธ์
        assertEquals(new BigDecimal("800"), sender.getBalance()); // 1000 - 200
        assertEquals(new BigDecimal("700"), receiver.getBalance()); // 500 + 200
        assertEquals(sender, returnedSender); // ตรวจสอบว่าคืน sender เดียวกัน

        // ตรวจสอบว่ามีการเรียก save ประวัติการทำรายการ 2 ครั้ง (ขาออก และ ขาเข้า)
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        // ตรวจสอบว่า saveAll ถูกเรียกด้วย list ที่มี sender และ receiver
        verify(walletRepository, times(1)).saveAll(anyList());
    }
    
}
