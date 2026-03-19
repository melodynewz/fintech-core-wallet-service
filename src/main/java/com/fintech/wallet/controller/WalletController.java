package com.fintech.wallet.controller;

import com.fintech.wallet.dto.request.TransactionRequest;
import com.fintech.wallet.dto.request.TransferRequest;
import com.fintech.wallet.dto.response.WalletResponse;
import com.fintech.wallet.model.entity.Wallet;
import com.fintech.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    // แยก V1 ไว้กรณีมีการเปลี่ยนแปลง API ใหม่หลังจากใช้มาสักพักจะได้ไม่กระทบ
    /* ถ้าไม่ใช้วิธีสร้าง controller ใหม่ ก็สามารถ
     * @RequestMapping("/api")
     * แล้วย้าย v1 ลงไปข้างล่างแทน
     * API เดิม (v1) @PostMapping("/v1/wallets/transfer")
     * API ใหม่ (v2) @PostMapping("/v2/wallets/transfer")
    */ 

    private final WalletService walletService;

    // 1. ดูยอดเงิน (Balance Inquiry)
    @GetMapping("/{accountNumber}")
    public ResponseEntity<WalletResponse> getBalance(@PathVariable String accountNumber) {
        Wallet wallet = walletService.getWalletDetails(accountNumber);
        return ResponseEntity.ok(convertToResponse(wallet));
    }

    // 2. ฝากเงิน (Deposit)
    @PostMapping("/deposit")
    public ResponseEntity<WalletResponse> deposit(@RequestBody TransactionRequest request) {
        Wallet wallet = walletService.deposit(request.getAccountNumber(), request.getAmount());
        return ResponseEntity.ok(convertToResponse(wallet));
    }

    // 3. ถอนเงิน (Withdraw)
    @PostMapping("/withdraw")
    public ResponseEntity<WalletResponse> withdraw(@RequestBody TransactionRequest request) {
        Wallet wallet = walletService.withdraw(request.getAccountNumber(), request.getAmount());
        return ResponseEntity.ok(convertToResponse(wallet));
    }

    // 4. โอนเงิน (Transfer)
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) {
        walletService.transfer(request.getFromAccountNumber(), request.getToAccountNumber(), request.getAmount());
        return ResponseEntity.ok("Transfer completed successfully");
    }

    // Helper method: เปลี่ยน Entity เป็น DTO (Senior Style)
    private WalletResponse convertToResponse(Wallet wallet) {
        return WalletResponse.builder()
                .accountNumber(wallet.getAccountNumber())
                .ownerName(wallet.getOwnerName())
                .balance(wallet.getBalance())
                .build();
    }
}