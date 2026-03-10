package dev.pafsmith.ledgerflow.transaction.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.pafsmith.ledgerflow.transaction.dto.CreateTransactionRequest;
import dev.pafsmith.ledgerflow.transaction.dto.TransactionResponse;
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
}
