package dev.pafsmith.ledgerflow.summary.dto;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Budget amount compared with actual spending")
public class BudgetVsActualResponse {
  private UUID budgetId;
  private UUID categoryId;
  private String categoryName;
  private String budgetName;
  private BigDecimal budgetLimit;
  private BigDecimal actualSpent;
  private BigDecimal remaining;
  private boolean overBudget;

  public BudgetVsActualResponse() {
  }

  public UUID getBudgetId() {
    return budgetId;
  }

  public void setBudgetId(UUID budgetId) {
    this.budgetId = budgetId;
  }

  public UUID getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(UUID categoryId) {
    this.categoryId = categoryId;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public String getBudgetName() {
    return budgetName;
  }

  public void setBudgetName(String budgetName) {
    this.budgetName = budgetName;
  }

  public BigDecimal getBudgetLimit() {
    return budgetLimit;
  }

  public void setBudgetLimit(BigDecimal budgetLimit) {
    this.budgetLimit = budgetLimit;
  }

  public BigDecimal getActualSpent() {
    return actualSpent;
  }

  public void setActualSpent(BigDecimal actualSpent) {
    this.actualSpent = actualSpent;
  }

  public BigDecimal getRemaining() {
    return remaining;
  }

  public void setRemaining(BigDecimal remaining) {
    this.remaining = remaining;
  }

  public boolean isOverBudget() {
    return overBudget;
  }

  public void setOverBudget(boolean overBudget) {
    this.overBudget = overBudget;
  }
}
