package com.fintech.wallet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fintech.wallet.exception.InsufficientBalanceException;
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
    void withdraw_InsufficientBalance_ShouldThrowException() {
        // Given
        when(walletRepository.findByAccountNumberForUpdate("12345")).thenReturn(Optional.of(mockWallet));

        // When & Then
        assertThrows(InsufficientBalanceException.class, () -> {
            walletService.withdraw("12345", new BigDecimal("2000.00"));
        });
        verify(walletRepository, never()).save(any()); // ต้องไม่มีการบันทึกเงินถ้าเงินไม่พอ
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
        walletService.transfer("12345", "67890", new BigDecimal("200.00"));

        // Then
        assertEquals(new BigDecimal("800.00"), mockWallet.getBalance());
        assertEquals(new BigDecimal("300.00"), receiverWallet.getBalance());
        verify(transactionRepository, times(2)).save(any()); // ต้องมีประวัติ 2 รายการ (ถอนและฝาก)
    }
}
