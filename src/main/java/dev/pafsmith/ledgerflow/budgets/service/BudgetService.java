package dev.pafsmith.ledgerflow.budgets.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.pafsmith.ledgerflow.budgets.dto.BudgetResponse;
import dev.pafsmith.ledgerflow.budgets.dto.CreateBudgetRequest;
import dev.pafsmith.ledgerflow.budgets.entity.Budget;
import dev.pafsmith.ledgerflow.budgets.repository.BudgetRepository;
import dev.pafsmith.ledgerflow.category.entity.Category;
import dev.pafsmith.ledgerflow.category.repository.CategoryRepository;
import dev.pafsmith.ledgerflow.common.exception.ForbiddenException;
import dev.pafsmith.ledgerflow.common.exception.ResourceNotFoundException;
import dev.pafsmith.ledgerflow.user.entity.User;
import dev.pafsmith.ledgerflow.user.repository.UserRepository;

@Service
public class BudgetService {

  private final BudgetRepository budgetRepository;

  private final CategoryRepository categoryRepository;

  private final UserRepository userRepository;

  public BudgetService(BudgetRepository budgetRepository, CategoryRepository categoryRepository,
      UserRepository userRepository) {
    this.budgetRepository = budgetRepository;
    this.categoryRepository = categoryRepository;
    this.userRepository = userRepository;
  }

  public List<BudgetResponse> getBudgetsForUser(UUID userId) {
    return budgetRepository
        .findByUserId(userId)
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  public List<Budget> getBudgetsForUserForMonth(UUID userId, Integer year, Integer month) {
    return budgetRepository.findByUserIdAndYearAndMonth(userId, year, month);
  }

  public Budget getBudgetForUserCategoryAndMonth(UUID userId, UUID categoryId, Integer year, Integer month) {
    return budgetRepository
        .findByUserIdAndCategoryIdAndYearAndMonth(userId, categoryId, year, month)
        .orElseThrow(() -> new RuntimeException("Budget not found"));
  }

  public BudgetResponse createBudget(CreateBudgetRequest request, UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    if (!category.getUser().getId().equals(user.getId())) {
      throw new ForbiddenException("Category does not belong to user");
    }

    Budget budget = new Budget();
    budget.setUser(user);
    budget.setCategory(category);
    budget.setYear(request.getYear());
    budget.setMonth(request.getMonth());
    budget.setLimitAmount(request.getLimitAmount());
    budget.setName(request.getName());

    Budget savedBudget = budgetRepository.save(budget);

    return mapToResponse(savedBudget);
  }

  private BudgetResponse mapToResponse(Budget budget) {
    BudgetResponse response = new BudgetResponse();
    response.setId(budget.getId());
    response.setUserId(budget.getUser().getId());
    response.setCategoryId(budget.getCategory().getId());
    response.setYear(budget.getYear());
    response.setMonth(budget.getMonth());
    response.setName(budget.getName());
    response.setLimitAmount(budget.getLimitAmount());
    response.setCreatedAt(budget.getCreatedAt());
    response.setUpdatedAt(budget.getUpdatedAt());
    return response;
  }
}
