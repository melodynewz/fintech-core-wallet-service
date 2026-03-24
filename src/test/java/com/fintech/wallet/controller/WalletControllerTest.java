package com.fintech.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.wallet.dto.request.TransactionRequest;
import com.fintech.wallet.dto.request.TransferRequest;
import com.fintech.wallet.dto.response.TransactionResponse;
import com.fintech.wallet.dto.response.WalletResponse;
import com.fintech.wallet.model.entity.Wallet;
import com.fintech.wallet.model.enums.TransactionType;
import com.fintech.wallet.service.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET /api/v1/wallets/{accountNumber} - valid account returns wallet")
    void getBalance_ValidAccount_ReturnsWallet() throws Exception {
        Wallet wallet = Wallet.builder()
                .accountNumber("12345")
                .ownerName("John Doe")
                .balance(new BigDecimal("1000.00"))
                .build();
        when(walletService.getWalletDetails("12345")).thenReturn(wallet);

        mockMvc.perform(get("/api/v1/wallets/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("12345"))
                .andExpect(jsonPath("$.ownerName").value("John Doe"))
                .andExpect(jsonPath("$.balance").value(1000.00));
    }

    @Test
    @DisplayName("GET /api/v1/wallets/{accountNumber} - invalid account format returns 400")
    void getBalance_InvalidAccountFormat_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/wallets/{accountNumber}/transactions - returns paginated transactions")
    void getTransactionHistory_ReturnsPage() throws Exception {
        TransactionResponse tx = TransactionResponse.builder()
                .id(1L)
                .walletId(1L)
                .accountNumber("12345")
                .amount(new BigDecimal("500.00"))
                .type(TransactionType.DEPOSIT)
                .transactionDate(LocalDateTime.now())
                .build();
        Page<TransactionResponse> page = new PageImpl<>(List.of(tx), PageRequest.of(0, 20), 1);
        when(walletService.getTransactionHistory(eq("12345"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/wallets/12345/transactions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].amount").value(500.00))
                .andExpect(jsonPath("$.content[0].type").value("DEPOSIT"));
    }

    @Test
    @DisplayName("POST /api/v1/wallets/deposit - valid request returns updated wallet")
    void deposit_ValidRequest_ReturnsWallet() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("12345");
        request.setAmount(new BigDecimal("200.00"));

        Wallet wallet = Wallet.builder()
                .accountNumber("12345")
                .ownerName("John Doe")
                .balance(new BigDecimal("1200.00"))
                .build();
        when(walletService.deposit("12345", new BigDecimal("200.00"))).thenReturn(wallet);

        mockMvc.perform(post("/api/v1/wallets/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("12345"))
                .andExpect(jsonPath("$.balance").value(1200.00));
    }

    @Test
    @DisplayName("POST /api/v1/wallets/deposit - invalid request returns 400")
    void deposit_InvalidRequest_ReturnsBadRequest() throws Exception {
        // missing amount
        mockMvc.perform(post("/api/v1/wallets/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountNumber\":\"12345\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/wallets/withdraw - valid request returns updated wallet")
    void withdraw_ValidRequest_ReturnsWallet() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("12345");
        request.setAmount(new BigDecimal("100.00"));

        Wallet wallet = Wallet.builder()
                .accountNumber("12345")
                .ownerName("John Doe")
                .balance(new BigDecimal("900.00"))
                .build();
        when(walletService.withdraw("12345", new BigDecimal("100.00"))).thenReturn(wallet);

        mockMvc.perform(post("/api/v1/wallets/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("12345"))
                .andExpect(jsonPath("$.balance").value(900.00));
    }

    @Test
    @DisplayName("POST /api/v1/wallets/transfer - valid request returns sender wallet")
    void transfer_ValidRequest_ReturnsWallet() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber("12345");
        request.setToAccountNumber("67890");
        request.setAmount(new BigDecimal("300.00"));

        Wallet wallet = Wallet.builder()
                .accountNumber("12345")
                .ownerName("John Doe")
                .balance(new BigDecimal("700.00"))
                .build();
        when(walletService.transfer("12345", "67890", new BigDecimal("300.00"))).thenReturn(wallet);

        mockMvc.perform(post("/api/v1/wallets/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("12345"))
                .andExpect(jsonPath("$.balance").value(700.00));
    }

    @Test
    @DisplayName("POST /api/v1/wallets/transfer - invalid request returns 400")
    void transfer_InvalidRequest_ReturnsBadRequest() throws Exception {
        // missing fields
        mockMvc.perform(post("/api/v1/wallets/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}