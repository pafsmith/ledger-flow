package dev.pafsmith.ledgerflow.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import dev.pafsmith.ledgerflow.transaction.enums.TransactionType;

public class CreateTransactionRequest {

  private UUID userId; // Pull From auth in the future
  private UUID accountId;
  private UUID categoryId;
  private UUID destinationAccountId;
  private String description;
  private BigDecimal amount;
  private TransactionType type;
  private LocalDate transactionDate;
  private String merchant;
  private String notes;

  public CreateTransactionRequest() {

  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public void setAccountId(UUID accountId) {
    this.accountId = accountId;
  }

  public UUID getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(UUID categoryId) {
    this.categoryId = categoryId;
  }

  public UUID getDestinationAccountId() {
    return destinationAccountId;
  }

  public void setDestinationAccountId(UUID destinationAccountId) {
    this.destinationAccountId = destinationAccountId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public TransactionType getType() {
    return type;
  }

  public void setType(TransactionType type) {
    this.type = type;
  }

  public LocalDate getTransactionDate() {
    return transactionDate;
  }

  public void setTransactionDate(LocalDate transactionDate) {
    this.transactionDate = transactionDate;
  }

  public String getMerchant() {
    return merchant;
  }

  public void setMerchant(String merchant) {
    this.merchant = merchant;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

}
