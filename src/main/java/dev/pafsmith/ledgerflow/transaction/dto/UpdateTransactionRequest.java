package dev.pafsmith.ledgerflow.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import dev.pafsmith.ledgerflow.transaction.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for updating a transaction")
public class UpdateTransactionRequest {
  // @NotNull(message = "User id is required")
  // private UUID userId;

  @NotNull(message = "Account id is required")
  private UUID accountId;
  private UUID categoryId;
  private UUID destinationAccountId;

  @NotBlank(message = "Description is required")
  @Size(max = 255, message = "Description must be 255 characters or fewer")
  private String description;

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
  private BigDecimal amount;

  @NotNull(message = "Transaction type is required")
  private TransactionType type;

  @NotNull(message = "Transaction date is required")
  private LocalDate transactionDate;

  @Size(max = 255, message = "Merchant must be 255 characters or fewer")
  private String merchant;

  private String notes;

  public UpdateTransactionRequest() {

  }

  // public UUID getUserId() {
  // return userId;
  // }
  //
  // public void setUserId(UUID userId) {
  // this.userId = userId;
  // }

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
