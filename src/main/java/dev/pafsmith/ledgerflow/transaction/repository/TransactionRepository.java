package dev.pafsmith.ledgerflow.transaction.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dev.pafsmith.ledgerflow.transaction.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

  List<Transaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
      UUID userId,
      LocalDate startDate,
      LocalDate endDate);
}
