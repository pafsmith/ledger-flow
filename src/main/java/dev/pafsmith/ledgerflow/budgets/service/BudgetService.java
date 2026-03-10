package dev.pafsmith.ledgerflow.budgets.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.pafsmith.ledgerflow.budgets.entity.Budget;
import dev.pafsmith.ledgerflow.budgets.repository.BudgetRepository;

@Service
public class BudgetService {

  private final BudgetRepository budgetRepository;

  public BudgetService(BudgetRepository budgetRepository) {
    this.budgetRepository = budgetRepository;
  }

  public List<Budget> getBudgetsForUser(UUID userId) {
    return budgetRepository.findByUserId(userId);
  }

  public List<Budget> getBudgetsForUserForMonth(UUID userId, Integer year, Integer month) {
    return budgetRepository.findByUserIdAndYearAndMonth(userId, year, month);
  }

  public Budget getBudgetForUserCategoryAndMonth(UUID userId, UUID categoryId, Integer year, Integer month) {
    return budgetRepository
        .findByUserIdAndCategoryIdAndYearAndMonth(userId, categoryId, year, month)
        .orElseThrow(() -> new RuntimeException("Budget not found"));
  }
}
