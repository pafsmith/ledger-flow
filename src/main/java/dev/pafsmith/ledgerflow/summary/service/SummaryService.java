package dev.pafsmith.ledgerflow.summary.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.pafsmith.ledgerflow.budgets.entity.Budget;
import dev.pafsmith.ledgerflow.budgets.repository.BudgetRepository;
import dev.pafsmith.ledgerflow.category.entity.Category;
import dev.pafsmith.ledgerflow.category.repository.CategoryRepository;
import dev.pafsmith.ledgerflow.common.exception.BadRequestException;
import dev.pafsmith.ledgerflow.common.exception.ResourceNotFoundException;
import dev.pafsmith.ledgerflow.summary.dto.BudgetVsActualResponse;
import dev.pafsmith.ledgerflow.summary.dto.CategorySpendResponse;
import dev.pafsmith.ledgerflow.summary.dto.MonthlySummaryResponse;
import dev.pafsmith.ledgerflow.transaction.entity.Transaction;
import dev.pafsmith.ledgerflow.transaction.enums.TransactionType;
import dev.pafsmith.ledgerflow.transaction.repository.TransactionRepository;
import dev.pafsmith.ledgerflow.user.repository.UserRepository;

@Service
public class SummaryService {
  private final TransactionRepository transactionRepository;
  private final BudgetRepository budgetRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;

  public SummaryService(
      TransactionRepository transactionRepository,
      BudgetRepository budgetRepository,
      CategoryRepository categoryRepository,
      UserRepository userRepository) {
    this.transactionRepository = transactionRepository;
    this.budgetRepository = budgetRepository;
    this.categoryRepository = categoryRepository;
    this.userRepository = userRepository;
  }

  public MonthlySummaryResponse getMonthlySummary(UUID userId, Integer year, Integer month) {
    validateRequest(year, month);

    userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

    List<Transaction> transactions = transactionRepository
        .findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, startDate, endDate);

    BigDecimal totalIncome = BigDecimal.ZERO;
    BigDecimal totalExpenses = BigDecimal.ZERO;
    Map<UUID, BigDecimal> expenseByCategory = new HashMap<>();

    for (Transaction transaction : transactions) {
      if (transaction.getType() == TransactionType.INCOME) {
        totalIncome = totalIncome.add(transaction.getAmount());
      }

      if (transaction.getType() == TransactionType.EXPENSE) {
        totalExpenses = totalExpenses.add(transaction.getAmount());

        if (transaction.getCategory() != null) {
          UUID categoryId = transaction.getCategory().getId();
          expenseByCategory.merge(categoryId, transaction.getAmount(), BigDecimal::add);
        }
      }
    }

    List<Budget> budgets = budgetRepository.findByUserIdAndYearAndMonth(userId, year, month);
    Map<UUID, String> categoryNames = buildCategoryNameMap(userId, expenseByCategory.keySet(), budgets);

    List<CategorySpendResponse> spendByCategory = expenseByCategory.entrySet().stream()
        .map(entry -> mapToCategorySpend(entry.getKey(), entry.getValue(), categoryNames))
        .sorted(Comparator.comparing(CategorySpendResponse::getAmount).reversed())
        .toList();

    List<BudgetVsActualResponse> budgetVsActual = budgets.stream()
        .map(budget -> mapToBudgetVsActual(budget, expenseByCategory, categoryNames))
        .sorted(Comparator.comparing(BudgetVsActualResponse::getCategoryName))
        .toList();

    MonthlySummaryResponse response = new MonthlySummaryResponse();
    response.setMonth(month);
    response.setYear(year);
    response.setTotalIncome(totalIncome);
    response.setTotalExpenses(totalExpenses);
    response.setNet(totalIncome.subtract(totalExpenses));
    response.setSpendByCategory(spendByCategory);
    response.setBudgetVsActual(budgetVsActual);
    return response;
  }

  private void validateRequest(Integer year, Integer month) {
    if (month == null || month < 1 || month > 12) {
      throw new BadRequestException("Month must be between 1 and 12");
    }

    if (year == null || year < 1) {
      throw new BadRequestException("Year must be greater than 0");
    }
  }

  private Map<UUID, String> buildCategoryNameMap(
      UUID userId,
      Set<UUID> expenseCategoryIds,
      List<Budget> budgets) {
    Set<UUID> categoryIds = new HashSet<>(expenseCategoryIds);

    for (Budget budget : budgets) {
      categoryIds.add(budget.getCategory().getId());
    }

    if (categoryIds.isEmpty()) {
      return Map.of();
    }

    return categoryRepository.findByUserId(userId)
        .stream()
        .filter(category -> categoryIds.contains(category.getId()))
        .collect(java.util.stream.Collectors.toMap(Category::getId, Category::getName));
  }

  private CategorySpendResponse mapToCategorySpend(
      UUID categoryId,
      BigDecimal amount,
      Map<UUID, String> categoryNames) {
    CategorySpendResponse response = new CategorySpendResponse();
    response.setCategoryId(categoryId);
    response.setCategoryName(categoryNames.getOrDefault(categoryId, "Unknown category"));
    response.setAmount(amount);
    return response;
  }

  private BudgetVsActualResponse mapToBudgetVsActual(
      Budget budget,
      Map<UUID, BigDecimal> expenseByCategory,
      Map<UUID, String> categoryNames) {
    UUID categoryId = budget.getCategory().getId();
    BigDecimal actualSpent = expenseByCategory.getOrDefault(categoryId, BigDecimal.ZERO);
    BigDecimal remaining = budget.getLimitAmount().subtract(actualSpent);

    BudgetVsActualResponse response = new BudgetVsActualResponse();
    response.setBudgetId(budget.getId());
    response.setCategoryId(categoryId);
    response.setCategoryName(categoryNames.getOrDefault(categoryId, "Unknown category"));
    response.setBudgetName(budget.getName());
    response.setBudgetLimit(budget.getLimitAmount());
    response.setActualSpent(actualSpent);
    response.setRemaining(remaining);
    response.setOverBudget(actualSpent.compareTo(budget.getLimitAmount()) > 0);
    return response;
  }
}
