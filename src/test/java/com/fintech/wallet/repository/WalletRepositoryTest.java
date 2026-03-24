package com.fintech.wallet.repository;

import com.fintech.wallet.model.entity.User;
import com.fintech.wallet.model.entity.Wallet;
import com.fintech.wallet.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class WalletRepositoryTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .role(UserRole.ROLE_USER)
                .build();
        userRepository.save(testUser);
    }

    private Wallet createWallet(String accountNumber, String ownerName, BigDecimal balance) {
        return Wallet.builder()
                .accountNumber(accountNumber)
                .ownerName(ownerName)
                .balance(balance)
                .user(testUser)
                .build();
    }

    @Test
    @DisplayName("findByAccountNumber - returns wallet when exists")
    void findByAccountNumber_ExistingWallet_ReturnsWallet() {
        Wallet wallet = createWallet("123456", "John Doe", new BigDecimal("1000.00"));
        walletRepository.save(wallet);

        Optional<Wallet> found = walletRepository.findByAccountNumber("123456");
        assertThat(found).isPresent();
        assertThat(found.get().getAccountNumber()).isEqualTo("123456");
        assertThat(found.get().getOwnerName()).isEqualTo("John Doe");
        assertThat(found.get().getBalance()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("findByAccountNumber - returns empty when not exists")
    void findByAccountNumber_NonExistingWallet_ReturnsEmpty() {
        Optional<Wallet> found = walletRepository.findByAccountNumber("999999");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findByAccountNumberForUpdate - returns wallet when exists")
    void findByAccountNumberForUpdate_ExistingWallet_ReturnsWallet() {
        Wallet wallet = createWallet("654321", "Jane Doe", new BigDecimal("500.00"));
        walletRepository.save(wallet);

        Optional<Wallet> found = walletRepository.findByAccountNumberForUpdate("654321");
        assertThat(found).isPresent();
        assertThat(found.get().getAccountNumber()).isEqualTo("654321");
    }

    @Test
    @DisplayName("findByAccountNumberForUpdate - returns empty when not exists")
    void findByAccountNumberForUpdate_NonExistingWallet_ReturnsEmpty() {
        Optional<Wallet> found = walletRepository.findByAccountNumberForUpdate("000000");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("save and retrieve wallet")
    void saveWallet_RetrieveSuccess() {
        Wallet wallet = createWallet("111222", "Alice", new BigDecimal("750.50"));
        Wallet saved = walletRepository.save(wallet);
        assertThat(saved.getId()).isNotNull();

        Wallet retrieved = walletRepository.findById(saved.getId()).orElseThrow();
        assertThat(retrieved.getAccountNumber()).isEqualTo("111222");
        assertThat(retrieved.getBalance()).isEqualByComparingTo("750.50");
        assertThat(retrieved.getUser()).isEqualTo(testUser);
    }
}