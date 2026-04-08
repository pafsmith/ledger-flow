package dev.pafsmith.ledgerflow.budgets.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateBudgetRequest {
  @NotNull(message = "Category id is required")
  private UUID categoryId;

  @NotBlank(message = "Name is required")
  @Size(max = 100, message = "Name must be 100 characters or fewer")
  private String name;

  @NotNull(message = "Limit amount is required")
  @DecimalMin(value = "0.00", message = "Limit amount must be at least 0")
  private BigDecimal limitAmount;

  @NotNull(message = "Year is required")
  @Min(value = 1, message = "Year must be greater than 0")
  private Integer year;

  @NotNull(message = "Month is required")
  @Min(value = 1, message = "Month must be between 1 and 12")
  @Max(value = 12, message = "Month must be between 1 and 12")
  private Integer month;

  public CreateBudgetRequest() {
  }

  public UUID getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(UUID categoryId) {
    this.categoryId = categoryId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BigDecimal getLimitAmount() {
    return limitAmount;
  }

  public void setLimitAmount(BigDecimal limitAmount) {
    this.limitAmount = limitAmount;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public Integer getMonth() {
    return month;
  }

  public void setMonth(Integer month) {
    this.month = month;
  }

}
