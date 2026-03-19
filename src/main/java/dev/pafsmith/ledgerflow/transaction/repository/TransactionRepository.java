package dev.pafsmith.ledgerflow.transaction.repository;

import java.util.List;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dev.pafsmith.ledgerflow.transaction.entity.Transaction;
import dev.pafsmith.ledgerflow.transaction.enums.TransactionType;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

  List<Transaction> findByUserIdOrderByTransactionDateDesc(UUID userId);

  List<Transaction> findByAccountIdOrderByTransactionDateDesc(UUID accountId);

  List<Transaction> findByCategoryIdOrderByTransactionDateDesc(UUID categoryId);

  List<Transaction> findByUserIdAndTypeOrderByTransactionDateDesc(UUID userId, TransactionType type);

  List<Transaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
      UUID userId,
      LocalDate startDate,
      LocalDate endDate);
}
