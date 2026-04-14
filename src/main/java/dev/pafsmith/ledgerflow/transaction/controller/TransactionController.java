package dev.pafsmith.ledgerflow.transaction.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
import dev.pafsmith.ledgerflow.transaction.dto.PagedTransactionResponse;
import dev.pafsmith.ledgerflow.transaction.dto.TransactionFilterRequest;
import dev.pafsmith.ledgerflow.transaction.dto.TransactionResponse;
import dev.pafsmith.ledgerflow.transaction.dto.UpdateTransactionRequest;
import dev.pafsmith.ledgerflow.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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
  public TransactionResponse createTransaction(@Valid @RequestBody CreateTransactionRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    return transactionService.createTransaction(request, userDetails.getUsername());
  }

  @GetMapping
  public PagedTransactionResponse getTransactions(
      TransactionFilterRequest filter,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "transactionDate") String sortBy,
      @RequestParam(defaultValue = "desc") String direction,
      @AuthenticationPrincipal UserDetails userDetails) {
    return transactionService.getTransactions(
        userDetails.getUsername(),
        filter,
        page,
        size,
        sortBy,
        direction);
  }

  @GetMapping("/{transactionId}")
  @Operation(summary = "Get transaction by id")
  public TransactionResponse getTransactionById(@PathVariable UUID transactionId,
      @AuthenticationPrincipal UserDetails userDetails) {
    return transactionService.getTransactionById(transactionId, userDetails.getUsername());
  }

  @PutMapping("/{transactionId}")
  @Operation(summary = "Update a transaction")
  public TransactionResponse updateTransaction(
      @PathVariable UUID transactionId,
      @Valid @RequestBody UpdateTransactionRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    return transactionService.updateTransaction(transactionId, request, userDetails.getUsername());
  }

  @DeleteMapping("/{transactionId}")
  @Operation(summary = "Delete a transaction")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTransaction(@PathVariable UUID transactionId,
      @AuthenticationPrincipal UserDetails userDetails) {
    transactionService.deleteTransaction(transactionId, userDetails.getUsername());
  }
}
