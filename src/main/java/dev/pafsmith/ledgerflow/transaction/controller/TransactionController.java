package dev.pafsmith.ledgerflow.transaction.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.pafsmith.ledgerflow.transaction.dto.CreateTransactionRequest;
import dev.pafsmith.ledgerflow.transaction.dto.TransactionResponse;
import dev.pafsmith.ledgerflow.transaction.dto.UpdateTransactionRequest;
import dev.pafsmith.ledgerflow.transaction.enums.TransactionType;
import dev.pafsmith.ledgerflow.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Operations for managing transactions")
public class TransactionController {

  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a transaction", description = "Creates a new income, expense, or transfer transaction", responses = {
      @ApiResponse(responseCode = "201", description = "Transaction created"),
      @ApiResponse(responseCode = "400", description = "Validation failed"),
      @ApiResponse(responseCode = "404", description = "Related resource not found")
  })
  public TransactionResponse createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
    return transactionService.createTransaction(request);
  }

  @GetMapping("/{transactionId}")
  @Operation(summary = "Get transaction by id")
  public TransactionResponse getTransactionById(@PathVariable UUID transactionId) {
    return transactionService.getTransactionById(transactionId);
  }

  @GetMapping("/user/{userId}")
  @Operation(summary = "Get all transactions for a user")
  public List<TransactionResponse> getTransactionsForUser(@PathVariable UUID userId) {
    return transactionService.getTransactionsForUser(userId);
  }

  @GetMapping("/account/{accountId}")
  @Operation(summary = "Get all transactions for an account")
  public List<TransactionResponse> getTransactionsForAccount(@PathVariable UUID accountId) {
    return transactionService.getTransactionsForAccount(accountId);
  }

  @GetMapping("/category/{categoryId}")
  @Operation(summary = "Get all transactions for a category")
  public List<TransactionResponse> getTransactionsForCategory(@PathVariable UUID categoryId) {
    return transactionService.getTransactionsForCategory(categoryId);
  }

  @GetMapping("/user/{userId}/type/{type}")

  @Operation(summary = "Get all transactions by user and type")
  public List<TransactionResponse> getTransactionsForUserByType(
      @PathVariable UUID userId,
      @PathVariable TransactionType type) {
    return transactionService.getTransactionsForUserByType(userId, type);
  }

  @GetMapping("/user/{userId}/date-range")
  @Operation(summary = "Get all transactions by user within a date range")
  public List<TransactionResponse> getTransactionsForUserBetweenDates(
      @PathVariable UUID userId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return transactionService.getTransactionsForUserBetweenDates(userId, startDate, endDate);
  }

  @PutMapping("/{transactionId}")
  @Operation(summary = "Update a transaction")
  public TransactionResponse updateTransaction(
      @PathVariable UUID transactionId,
      @Valid @RequestBody UpdateTransactionRequest request) {
    return transactionService.updateTransaction(transactionId, request);
  }

  @DeleteMapping("/{transactionId}")
  @Operation(summary = "Delete a transaction")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTransaction(@PathVariable UUID transactionId) {
    transactionService.deleteTransaction(transactionId);
  }
}
