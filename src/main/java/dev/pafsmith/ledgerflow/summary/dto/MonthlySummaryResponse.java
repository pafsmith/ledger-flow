package dev.pafsmith.ledgerflow.summary.dto;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Monthly financial summary")
public class MonthlySummaryResponse {
  private Integer month;
  private Integer year;
  private BigDecimal totalIncome;
  private BigDecimal totalExpenses;
  private BigDecimal net;
  private List<CategorySpendResponse> spendByCategory;
  private List<BudgetVsActualResponse> budgetVsActual;

  public MonthlySummaryResponse() {
  }

  public Integer getMonth() {
    return month;
  }

  public void setMonth(Integer month) {
    this.month = month;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public BigDecimal getTotalIncome() {
    return totalIncome;
  }

  public void setTotalIncome(BigDecimal totalIncome) {
    this.totalIncome = totalIncome;
  }

  public BigDecimal getTotalExpenses() {
    return totalExpenses;
  }

  public void setTotalExpenses(BigDecimal totalExpenses) {
    this.totalExpenses = totalExpenses;
  }

  public BigDecimal getNet() {
    return net;
  }

  public void setNet(BigDecimal net) {
    this.net = net;
  }

  public List<CategorySpendResponse> getSpendByCategory() {
    return spendByCategory;
  }

  public void setSpendByCategory(List<CategorySpendResponse> spendByCategory) {
    this.spendByCategory = spendByCategory;
  }

  public List<BudgetVsActualResponse> getBudgetVsActual() {
    return budgetVsActual;
  }

  public void setBudgetVsActual(List<BudgetVsActualResponse> budgetVsActual) {
    this.budgetVsActual = budgetVsActual;
  }
}
