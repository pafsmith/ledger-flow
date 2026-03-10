package dev.pafsmith.ledgerflow.transaction.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.pafsmith.ledgerflow.transaction.entity.Transaction;
import dev.pafsmith.ledgerflow.transaction.enums.TransactionType;
import dev.pafsmith.ledgerflow.transaction.repository.TransactionRepository;

@Service
public class TransactionService {

  private final TransactionRepository transactionRepository;

  public TransactionService(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public List<Transaction> getTransactionsForUser(UUID userId) {
    return transactionRepository.findByUserId(userId);
  }

  public List<Transaction> getTransactionsForAccount(UUID accountId) {
    return transactionRepository.findByAccountId(accountId);
  }

  public List<Transaction> getTransactionsForCategory(UUID categoryId) {
    return transactionRepository.findByCategoryId(categoryId);
  }

  public List<Transaction> getTransactionsForUserByType(UUID userId, TransactionType type) {
    return transactionRepository.findByUserIdAndType(userId, type);
  }

  public List<Transaction> getTransactionsForUserBetweenDates(
      UUID userId,
      LocalDate startDate,
      LocalDate endDate) {
    return transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate);
  }

}
