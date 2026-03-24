package com.fintech.wallet.repository;

import com.fintech.wallet.model.entity.Transaction;
import com.fintech.wallet.model.entity.User;
import com.fintech.wallet.model.entity.Wallet;
import com.fintech.wallet.model.enums.TransactionType;
import com.fintech.wallet.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

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
        Wallet wallet = Wallet.builder()
                .accountNumber(accountNumber)
                .ownerName(ownerName)
                .balance(balance)
                .user(testUser)
                .build();
        return walletRepository.save(wallet);
    }

    @Test
    @DisplayName("findByWalletIdOrderByTransactionDateDesc - returns transactions sorted")
    void findByWalletIdOrderByTransactionDateDesc_ReturnsSorted() {
        Wallet wallet = createWallet("123456", "Test", BigDecimal.ZERO);

        Transaction tx1 = Transaction.builder()
                .wallet(wallet)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .transactionDate(LocalDateTime.now().minusHours(2))
                .build();
        Transaction tx2 = Transaction.builder()
                .wallet(wallet)
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.WITHDRAW)
                .transactionDate(LocalDateTime.now().minusHours(1))
                .build();
        transactionRepository.save(tx1);
        transactionRepository.save(tx2);

        List<Transaction> transactions = transactionRepository.findByWalletIdOrderByTransactionDateDesc(wallet.getId());
        assertThat(transactions).hasSize(2);
        assertThat(transactions.get(0).getAmount()).isEqualByComparingTo("50.00"); // later date first
        assertThat(transactions.get(1).getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("findByWalletId with pageable returns paginated results")
    void findByWalletId_Pageable_ReturnsPage() {
        Wallet wallet = createWallet("654321", "Jane", BigDecimal.ZERO);

        for (int i = 0; i < 5; i++) {
            Transaction tx = Transaction.builder()
                    .wallet(wallet)
                    .amount(new BigDecimal(i * 10))
                    .type(TransactionType.DEPOSIT)
                    .transactionDate(LocalDateTime.now().minusMinutes(i))
                    .build();
            transactionRepository.save(tx);
        }

        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "transactionDate"));
        Page<Transaction> page = transactionRepository.findByWalletId(wallet.getId(), pageable);

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("save transaction with wallet relationship")
    void saveTransaction_WithWallet_Success() {
        Wallet wallet = createWallet("777777", "Bob", BigDecimal.ZERO);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(new BigDecimal("200.00"))
                .type(TransactionType.TRANSFER)
                .transactionDate(LocalDateTime.now())
                .build();
        Transaction saved = transactionRepository.save(transaction);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getWallet().getId()).isEqualTo(wallet.getId());
        assertThat(saved.getAmount()).isEqualByComparingTo("200.00");
        assertThat(saved.getType()).isEqualTo(TransactionType.TRANSFER);
    }

    @Test
    @DisplayName("findByWalletId returns empty list for non-existing wallet")
    void findByWalletId_NonExistingWallet_ReturnsEmpty() {
        List<Transaction> transactions = transactionRepository.findByWalletIdOrderByTransactionDateDesc(999L);
        assertThat(transactions).isEmpty();
    }
}