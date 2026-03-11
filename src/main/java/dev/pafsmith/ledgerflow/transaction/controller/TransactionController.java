package dev.pafsmith.ledgerflow.transaction.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.pafsmith.ledgerflow.transaction.dto.CreateTransactionRequest;
import dev.pafsmith.ledgerflow.transaction.dto.TransactionResponse;
import dev.pafsmith.ledgerflow.transaction.enums.TransactionType;
import dev.pafsmith.ledgerflow.transaction.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TransactionResponse createTransaction(@RequestBody CreateTransactionRequest request) {
    return transactionService.createTransaction(request);
  }

  @GetMapping("/{transactionId}")
  public TransactionResponse getTransactionById(@PathVariable UUID transactionId) {
    return transactionService.getTransactionById(transactionId);
  }

  @GetMapping("/user/{userId}")
  public List<TransactionResponse> getTransactionsForUser(@PathVariable UUID userId) {
    return transactionService.getTransactionsForUser(userId);
  }

  @GetMapping("/account/{accountId}")
  public List<TransactionResponse> getTransactionsForAccount(@PathVariable UUID accountId) {
    return transactionService.getTransactionsForAccount(accountId);
  }

  @GetMapping("/category/{categoryId}")
  public List<TransactionResponse> getTransactionsForCategory(@PathVariable UUID categoryId) {
    return transactionService.getTransactionsForCategory(categoryId);
  }

  @GetMapping("/user/{userId}/type/{type}")
  public List<TransactionResponse> getTransactionsForUserByType(
      @PathVariable UUID userId,
      @PathVariable TransactionType type) {
    return transactionService.getTransactionsForUserByType(userId, type);
  }

  @GetMapping("/user/{userId}/date-range")
  public List<TransactionResponse> getTransactionsForUserBetweenDates(
      @PathVariable UUID userId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return transactionService.getTransactionsForUserBetweenDates(userId, startDate, endDate);
  }
}
