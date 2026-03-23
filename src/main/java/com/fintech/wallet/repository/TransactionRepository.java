package com.fintech.wallet.repository;

import com.fintech.wallet.model.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByWalletIdOrderByTransactionDateDesc(Long walletId);
    
    Page<Transaction> findByWalletId(Long walletId, Pageable pageable);
}