package com.fintech.wallet.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.wallet.dto.request.TransactionRequest;
import com.fintech.wallet.dto.request.TransferRequest;
import com.fintech.wallet.dto.response.LoginResponse;
import com.fintech.wallet.dto.response.WalletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WalletIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    private static final AtomicInteger IP_SUFFIX = new AtomicInteger(10);

    @BeforeEach
    void setUp() throws Exception {
        // Login to obtain token
        String loginBody = "{\"username\":\"demo\",\"password\":\"password\"}";
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        // avoid tripping rate limit across repeated test logins
                        .header("X-Forwarded-For", "192.168.0." + IP_SUFFIX.incrementAndGet())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(content, LoginResponse.class);
        authToken = loginResponse.getToken();
    }

    @Test
    @DisplayName("End-to-end: Get wallet balance")
    void getWalletBalance_ReturnsWallet() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/12345")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("12345"))
                .andExpect(jsonPath("$.ownerName").isNotEmpty())
                .andExpect(jsonPath("$.balance").isNumber());
    }

    @Test
    @DisplayName("End-to-end: Deposit money")
    void depositMoney_IncreasesBalance() throws Exception {
        // First get current balance
        MvcResult getResult = mockMvc.perform(get("/api/v1/wallets/12345")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        WalletResponse walletBefore = objectMapper.readValue(getResult.getResponse().getContentAsString(), WalletResponse.class);
        BigDecimal beforeBalance = walletBefore.getBalance();

        // Deposit 50.00
        TransactionRequest depositRequest = new TransactionRequest();
        depositRequest.setAccountNumber("12345");
        depositRequest.setAmount(new BigDecimal("50.00"));

        mockMvc.perform(post("/api/v1/wallets/deposit")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("12345"))
                .andExpect(jsonPath("$.balance").value(beforeBalance.add(new BigDecimal("50.00")).doubleValue()));
    }

    @Test
    @DisplayName("End-to-end: Withdraw money")
    void withdrawMoney_DecreasesBalance() throws Exception {
        // Ensure sufficient balance by depositing first
        TransactionRequest depositRequest = new TransactionRequest();
        depositRequest.setAccountNumber("12345");
        depositRequest.setAmount(new BigDecimal("100.00"));
        mockMvc.perform(post("/api/v1/wallets/deposit")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk());

        // Get balance after deposit
        MvcResult getResult = mockMvc.perform(get("/api/v1/wallets/12345")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        WalletResponse walletBefore = objectMapper.readValue(getResult.getResponse().getContentAsString(), WalletResponse.class);
        BigDecimal beforeBalance = walletBefore.getBalance();

        // Withdraw 30.00
        TransactionRequest withdrawRequest = new TransactionRequest();
        withdrawRequest.setAccountNumber("12345");
        withdrawRequest.setAmount(new BigDecimal("30.00"));

        mockMvc.perform(post("/api/v1/wallets/withdraw")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("12345"))
                .andExpect(jsonPath("$.balance").value(beforeBalance.subtract(new BigDecimal("30.00")).doubleValue()));
    }

    @Test
    @DisplayName("End-to-end: Transfer money between wallets")
    void transferMoney_UpdatesBothBalances() throws Exception {
        // Ensure sufficient balance in source wallet
        TransactionRequest depositRequest = new TransactionRequest();
        depositRequest.setAccountNumber("12345");
        depositRequest.setAmount(new BigDecimal("200.00"));
        mockMvc.perform(post("/api/v1/wallets/deposit")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk());

        // Get balances before transfer
        MvcResult sourceBefore = mockMvc.perform(get("/api/v1/wallets/12345")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        WalletResponse sourceWallet = objectMapper.readValue(sourceBefore.getResponse().getContentAsString(), WalletResponse.class);
        BigDecimal sourceBalanceBefore = sourceWallet.getBalance();

        MvcResult destBefore = mockMvc.perform(get("/api/v1/wallets/67890")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        WalletResponse destWallet = objectMapper.readValue(destBefore.getResponse().getContentAsString(), WalletResponse.class);
        BigDecimal destBalanceBefore = destWallet.getBalance();

        // Transfer 75.00
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountNumber("12345");
        transferRequest.setToAccountNumber("67890");
        transferRequest.setAmount(new BigDecimal("75.00"));

        mockMvc.perform(post("/api/v1/wallets/transfer")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("12345"))
                .andExpect(jsonPath("$.balance").value(sourceBalanceBefore.subtract(new BigDecimal("75.00")).doubleValue()));

        // Verify destination wallet balance increased
        MvcResult destAfter = mockMvc.perform(get("/api/v1/wallets/67890")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        WalletResponse destAfterWallet = objectMapper.readValue(destAfter.getResponse().getContentAsString(), WalletResponse.class);
        BigDecimal destBalanceAfter = destAfterWallet.getBalance();
        assert destBalanceAfter.equals(destBalanceBefore.add(new BigDecimal("75.00")));
    }

    @Test
    @DisplayName("End-to-end: Get transaction history paginated")
    void getTransactionHistory_ReturnsPaginated() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/12345/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }
}