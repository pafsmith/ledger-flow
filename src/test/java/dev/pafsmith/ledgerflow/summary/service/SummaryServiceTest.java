package dev.pafsmith.ledgerflow.summary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.pafsmith.ledgerflow.budgets.entity.Budget;
import dev.pafsmith.ledgerflow.budgets.repository.BudgetRepository;
import dev.pafsmith.ledgerflow.category.entity.Category;
import dev.pafsmith.ledgerflow.category.enums.CategoryType;
import dev.pafsmith.ledgerflow.category.repository.CategoryRepository;
import dev.pafsmith.ledgerflow.common.exception.BadRequestException;
import dev.pafsmith.ledgerflow.common.exception.ResourceNotFoundException;
import dev.pafsmith.ledgerflow.transaction.entity.Transaction;
import dev.pafsmith.ledgerflow.transaction.enums.TransactionType;
import dev.pafsmith.ledgerflow.transaction.repository.TransactionRepository;
import dev.pafsmith.ledgerflow.user.entity.User;
import dev.pafsmith.ledgerflow.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private BudgetRepository budgetRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private SummaryService summaryService;

  private UUID userId;
  private User user;
  private Category groceriesCategory;
  private Category utilitiesCategory;
  private Category salaryCategory;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();

    user = new User();
    user.setId(userId);
    user.setFirstName("Paul");
    user.setLastName("Smith");
    user.setEmail("paul@test.com");
    user.setPasswordHash("hash");

    groceriesCategory = createCategory("Groceries", CategoryType.EXPENSE);
    utilitiesCategory = createCategory("Utilities", CategoryType.EXPENSE);
    salaryCategory = createCategory("Salary", CategoryType.INCOME);
  }

  @Test
  void getMonthlySummary_shouldReturnSummaryWithTotalsCategorySpendAndBudgetComparison() {
    int year = 2026;
    int month = 4;

    Transaction income = createTransaction(TransactionType.INCOME, new BigDecimal("3000.00"), salaryCategory);
    Transaction groceriesExpense = createTransaction(TransactionType.EXPENSE, new BigDecimal("500.00"), groceriesCategory);
    Transaction utilitiesExpense = createTransaction(TransactionType.EXPENSE, new BigDecimal("200.00"), utilitiesCategory);
    Transaction transfer = createTransaction(TransactionType.TRANSFER, new BigDecimal("100.00"), null);

    Budget groceriesBudget = createBudget("Groceries budget", new BigDecimal("400.00"), year, month, groceriesCategory);
    Budget utilitiesBudget = createBudget("Utilities budget", new BigDecimal("250.00"), year, month, utilitiesCategory);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
        userId,
        LocalDate.of(2026, 4, 1),
        LocalDate.of(2026, 4, 30)))
        .thenReturn(List.of(income, groceriesExpense, utilitiesExpense, transfer));
    when(budgetRepository.findByUserIdAndYearAndMonth(userId, year, month))
        .thenReturn(List.of(groceriesBudget, utilitiesBudget));
    when(categoryRepository.findByUserId(userId)).thenReturn(List.of(groceriesCategory, utilitiesCategory, salaryCategory));

    var response = summaryService.getMonthlySummary(userId, year, month);

    assertThat(response.getTotalIncome()).isEqualByComparingTo("3000.00");
    assertThat(response.getTotalExpenses()).isEqualByComparingTo("700.00");
    assertThat(response.getNet()).isEqualByComparingTo("2300.00");

    assertThat(response.getSpendByCategory()).hasSize(2);
    assertThat(response.getSpendByCategory().getFirst().getCategoryName()).isEqualTo("Groceries");
    assertThat(response.getSpendByCategory().getFirst().getAmount()).isEqualByComparingTo("500.00");
    assertThat(response.getSpendByCategory().get(1).getCategoryName()).isEqualTo("Utilities");
    assertThat(response.getSpendByCategory().get(1).getAmount()).isEqualByComparingTo("200.00");

    assertThat(response.getBudgetVsActual()).hasSize(2);

    var groceriesComparison = response.getBudgetVsActual().stream()
        .filter(item -> item.getCategoryId().equals(groceriesCategory.getId()))
        .findFirst()
        .orElseThrow();

    assertThat(groceriesComparison.getBudgetLimit()).isEqualByComparingTo("400.00");
    assertThat(groceriesComparison.getActualSpent()).isEqualByComparingTo("500.00");
    assertThat(groceriesComparison.getRemaining()).isEqualByComparingTo("-100.00");
    assertThat(groceriesComparison.isOverBudget()).isTrue();
  }

  @Test
  void getMonthlySummary_shouldReturnBudgetComparisonWithZeroActualWhenNoExpensesExist() {
    int year = 2026;
    int month = 4;

    Budget groceriesBudget = createBudget("Groceries budget", new BigDecimal("400.00"), year, month, groceriesCategory);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
        userId,
        LocalDate.of(2026, 4, 1),
        LocalDate.of(2026, 4, 30)))
        .thenReturn(List.of());
    when(budgetRepository.findByUserIdAndYearAndMonth(userId, year, month))
        .thenReturn(List.of(groceriesBudget));
    when(categoryRepository.findByUserId(userId)).thenReturn(List.of(groceriesCategory));

    var response = summaryService.getMonthlySummary(userId, year, month);

    assertThat(response.getTotalIncome()).isEqualByComparingTo("0.00");
    assertThat(response.getTotalExpenses()).isEqualByComparingTo("0.00");
    assertThat(response.getNet()).isEqualByComparingTo("0.00");
    assertThat(response.getSpendByCategory()).isEmpty();

    assertThat(response.getBudgetVsActual()).hasSize(1);
    assertThat(response.getBudgetVsActual().getFirst().getActualSpent()).isEqualByComparingTo("0.00");
    assertThat(response.getBudgetVsActual().getFirst().getRemaining()).isEqualByComparingTo("400.00");
    assertThat(response.getBudgetVsActual().getFirst().isOverBudget()).isFalse();
  }

  @Test
  void getMonthlySummary_shouldThrowWhenMonthIsInvalid() {
    assertThatThrownBy(() -> summaryService.getMonthlySummary(userId, 2026, 13))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Month must be between 1 and 12");
  }

  @Test
  void getMonthlySummary_shouldThrowWhenYearIsInvalid() {
    assertThatThrownBy(() -> summaryService.getMonthlySummary(userId, 0, 4))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Year must be greater than 0");
  }

  @Test
  void getMonthlySummary_shouldThrowWhenUserNotFound() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> summaryService.getMonthlySummary(userId, 2026, 4))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found");
  }

  private Category createCategory(String name, CategoryType type) {
    Category category = new Category();
    category.setId(UUID.randomUUID());
    category.setUser(user);
    category.setName(name);
    category.setType(type);
    return category;
  }

  private Transaction createTransaction(TransactionType type, BigDecimal amount, Category category) {
    Transaction transaction = new Transaction();
    transaction.setId(UUID.randomUUID());
    transaction.setUser(user);
    transaction.setCategory(category);
    transaction.setType(type);
    transaction.setAmount(amount);
    transaction.setTransactionDate(LocalDate.of(2026, 4, 5));
    transaction.setDescription("Transaction");
    return transaction;
  }

  private Budget createBudget(String name, BigDecimal limitAmount, int year, int month, Category category) {
    Budget budget = new Budget();
    budget.setId(UUID.randomUUID());
    budget.setUser(user);
    budget.setCategory(category);
    budget.setName(name);
    budget.setLimitAmount(limitAmount);
    budget.setYear(year);
    budget.setMonth(month);
    return budget;
  }
}
