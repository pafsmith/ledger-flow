package dev.pafsmith.ledgerflow.transaction.repository;

import java.util.List;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.pafsmith.ledgerflow.transaction.entity.Transaction;
import dev.pafsmith.ledgerflow.transaction.enums.TransactionType;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

  List<Transaction> findByUserId(UUID userId);

  List<Transaction> findByAccountId(UUID accountId);

  List<Transaction> findByCategoryId(UUID categoryId);

  List<Transaction> findByUserIdAndType(UUID userId, TransactionType type);

  List<Transaction> findByUserIdAndTransactionDateBetween(
      UUID userId,
      LocalDate startDate,
      LocalDate endDate);
}
