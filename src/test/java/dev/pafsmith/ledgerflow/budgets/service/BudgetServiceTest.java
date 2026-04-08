package dev.pafsmith.ledgerflow.budgets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.pafsmith.ledgerflow.budgets.dto.CreateBudgetRequest;
import dev.pafsmith.ledgerflow.budgets.dto.UpdateBudgetRequest;
import dev.pafsmith.ledgerflow.budgets.entity.Budget;
import dev.pafsmith.ledgerflow.budgets.repository.BudgetRepository;
import dev.pafsmith.ledgerflow.category.entity.Category;
import dev.pafsmith.ledgerflow.category.repository.CategoryRepository;
import dev.pafsmith.ledgerflow.common.exception.BadRequestException;
import dev.pafsmith.ledgerflow.common.exception.ForbiddenException;
import dev.pafsmith.ledgerflow.common.exception.ResourceNotFoundException;
import dev.pafsmith.ledgerflow.user.entity.User;
import dev.pafsmith.ledgerflow.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

  @Mock
  private BudgetRepository budgetRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private BudgetService budgetService;

  private UUID userId;
  private UUID categoryId;
  private User user;
  private Category category;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    categoryId = UUID.randomUUID();

    user = new User();
    user.setId(userId);
    user.setFirstName("Paul");
    user.setLastName("Smith");
    user.setEmail("paul@test.com");
    user.setPasswordHash("hashed");

    category = new Category();
    category.setId(categoryId);
    category.setUser(user);
    category.setName("Groceries");
  }

  @Test
  void createBudget_shouldSaveBudgetSuccessfully() {
    CreateBudgetRequest request = new CreateBudgetRequest();
    request.setCategoryId(categoryId);
    request.setName("  Groceries  ");
    request.setLimitAmount(new BigDecimal("500.00"));
    request.setYear(2026);
    request.setMonth(4);

    UUID budgetId = UUID.randomUUID();
    Instant now = Instant.now();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
    when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> {
      Budget budget = invocation.getArgument(0);
      budget.setId(budgetId);
      budget.setCreatedAt(now);
      budget.setUpdatedAt(now);
      return budget;
    });

    var response = budgetService.createBudget(request, userId);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(budgetId);
    assertThat(response.getUserId()).isEqualTo(userId);
    assertThat(response.getCategoryId()).isEqualTo(categoryId);
    assertThat(response.getName()).isEqualTo("Groceries");
    assertThat(response.getYear()).isEqualTo(2026);
    assertThat(response.getMonth()).isEqualTo(4);
    assertThat(response.getCreatedAt()).isEqualTo(now);
    assertThat(response.getUpdatedAt()).isEqualTo(now);

    ArgumentCaptor<Budget> captor = ArgumentCaptor.forClass(Budget.class);
    verify(budgetRepository).save(captor.capture());

    Budget savedBudget = captor.getValue();
    assertThat(savedBudget.getUser()).isEqualTo(user);
    assertThat(savedBudget.getCategory()).isEqualTo(category);
    assertThat(savedBudget.getName()).isEqualTo("Groceries");
    assertThat(savedBudget.getLimitAmount()).isEqualByComparingTo("500.00");
    assertThat(savedBudget.getYear()).isEqualTo(2026);
    assertThat(savedBudget.getMonth()).isEqualTo(4);
  }

  @Test
  void createBudget_shouldThrowWhenDuplicateBudgetExists() {
    CreateBudgetRequest request = new CreateBudgetRequest();
    request.setCategoryId(categoryId);
    request.setName("Groceries");
    request.setLimitAmount(new BigDecimal("500.00"));
    request.setYear(2026);
    request.setMonth(4);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
    when(budgetRepository.existsByUserIdAndCategoryIdAndYearAndMonth(userId, categoryId, 2026, 4))
        .thenReturn(true);

    assertThatThrownBy(() -> budgetService.createBudget(request, userId))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Budget already exists for this category and period");

    verify(budgetRepository, never()).save(any(Budget.class));
  }

  @Test
  void createBudget_shouldThrowWhenUserNotFound() {
    CreateBudgetRequest request = new CreateBudgetRequest();
    request.setCategoryId(categoryId);
    request.setName("Groceries");
    request.setLimitAmount(new BigDecimal("500.00"));
    request.setYear(2026);
    request.setMonth(4);

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> budgetService.createBudget(request, userId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found");

    verify(categoryRepository, never()).findById(any(UUID.class));
    verify(budgetRepository, never()).save(any(Budget.class));
  }

  @Test
  void createBudget_shouldThrowWhenCategoryNotFound() {
    CreateBudgetRequest request = new CreateBudgetRequest();
    request.setCategoryId(categoryId);
    request.setName("Groceries");
    request.setLimitAmount(new BigDecimal("500.00"));
    request.setYear(2026);
    request.setMonth(4);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> budgetService.createBudget(request, userId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Category not found");

    verify(budgetRepository, never()).save(any(Budget.class));
  }

  @Test
  void createBudget_shouldThrowForbiddenWhenCategoryNotOwnedByUser() {
    CreateBudgetRequest request = new CreateBudgetRequest();
    request.setCategoryId(categoryId);
    request.setName("Groceries");
    request.setLimitAmount(new BigDecimal("500.00"));
    request.setYear(2026);
    request.setMonth(4);

    User otherUser = new User();
    otherUser.setId(UUID.randomUUID());

    Category otherUsersCategory = new Category();
    otherUsersCategory.setId(categoryId);
    otherUsersCategory.setUser(otherUser);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(otherUsersCategory));

    assertThatThrownBy(() -> budgetService.createBudget(request, userId))
        .isInstanceOf(ForbiddenException.class)
        .hasMessage("Category does not belong to user");

    verify(budgetRepository, never()).save(any(Budget.class));
  }

  @Test
  void getBudgetsForUser_shouldReturnAllBudgetsWhenNoFiltersProvided() {
    Budget budget = createBudget(2026, 4);
    when(budgetRepository.findByUserId(userId)).thenReturn(List.of(budget));

    var response = budgetService.getBudgetsForUser(userId, null, null);

    assertThat(response).hasSize(1);
    assertThat(response.getFirst().getId()).isEqualTo(budget.getId());
    assertThat(response.getFirst().getUserId()).isEqualTo(userId);
    assertThat(response.getFirst().getCategoryId()).isEqualTo(categoryId);

    verify(budgetRepository).findByUserId(userId);
    verify(budgetRepository, never()).findByUserIdAndYear(any(UUID.class), any(Integer.class));
    verify(budgetRepository, never()).findByUserIdAndMonth(any(UUID.class), any(Integer.class));
    verify(budgetRepository, never()).findByUserIdAndYearAndMonth(any(UUID.class), any(Integer.class),
        any(Integer.class));
  }

  @Test
  void getBudgetsForUser_shouldFilterByYearWhenOnlyYearProvided() {
    int year = 2026;
    Budget budget = createBudget(year, 4);
    when(budgetRepository.findByUserIdAndYear(userId, year)).thenReturn(List.of(budget));

    var response = budgetService.getBudgetsForUser(userId, year, null);

    assertThat(response).hasSize(1);
    assertThat(response.getFirst().getYear()).isEqualTo(year);

    verify(budgetRepository).findByUserIdAndYear(userId, year);
    verify(budgetRepository, never()).findByUserId(any(UUID.class));
    verify(budgetRepository, never()).findByUserIdAndMonth(any(UUID.class), any(Integer.class));
    verify(budgetRepository, never()).findByUserIdAndYearAndMonth(any(UUID.class), any(Integer.class),
        any(Integer.class));
  }

  @Test
  void getBudgetsForUser_shouldFilterByMonthWhenOnlyMonthProvided() {
    int month = 4;
    Budget budget = createBudget(2026, month);
    when(budgetRepository.findByUserIdAndMonth(userId, month)).thenReturn(List.of(budget));

    var response = budgetService.getBudgetsForUser(userId, null, month);

    assertThat(response).hasSize(1);
    assertThat(response.getFirst().getMonth()).isEqualTo(month);

    verify(budgetRepository).findByUserIdAndMonth(userId, month);
    verify(budgetRepository, never()).findByUserId(any(UUID.class));
    verify(budgetRepository, never()).findByUserIdAndYear(any(UUID.class), any(Integer.class));
    verify(budgetRepository, never()).findByUserIdAndYearAndMonth(any(UUID.class), any(Integer.class),
        any(Integer.class));
  }

  @Test
  void getBudgetsForUser_shouldFilterByYearAndMonthWhenBothProvided() {
    int year = 2026;
    int month = 4;
    Budget budget = createBudget(year, month);
    when(budgetRepository.findByUserIdAndYearAndMonth(userId, year, month)).thenReturn(List.of(budget));

    var response = budgetService.getBudgetsForUser(userId, year, month);

    assertThat(response).hasSize(1);
    assertThat(response.getFirst().getYear()).isEqualTo(year);
    assertThat(response.getFirst().getMonth()).isEqualTo(month);

    verify(budgetRepository).findByUserIdAndYearAndMonth(userId, year, month);
    verify(budgetRepository, never()).findByUserId(any(UUID.class));
    verify(budgetRepository, never()).findByUserIdAndYear(any(UUID.class), any(Integer.class));
    verify(budgetRepository, never()).findByUserIdAndMonth(any(UUID.class), any(Integer.class));
  }

  @Test
  void getBudgetsForUser_shouldThrowBadRequestWhenMonthIsOutOfRange() {
    assertThatThrownBy(() -> budgetService.getBudgetsForUser(userId, 2026, 13))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Month must be between 1 and 12");

    verifyNoInteractions(budgetRepository);
  }

  @Test
  void updateBudget_shouldUpdateBudgetSuccessfully() {
    UUID budgetId = UUID.randomUUID();

    Budget existingBudget = createBudget(2026, 4);
    existingBudget.setId(budgetId);

    UpdateBudgetRequest request = new UpdateBudgetRequest();
    request.setCategoryId(categoryId);
    request.setName("  Updated Groceries  ");
    request.setLimitAmount(new BigDecimal("600.00"));
    request.setYear(2026);
    request.setMonth(5);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(existingBudget));
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
    when(budgetRepository.existsByUserIdAndCategoryIdAndYearAndMonthAndIdNot(userId, categoryId, 2026, 5, budgetId))
        .thenReturn(false);
    when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var response = budgetService.updateBudget(budgetId, request, userId);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(budgetId);
    assertThat(response.getUserId()).isEqualTo(userId);
    assertThat(response.getCategoryId()).isEqualTo(categoryId);
    assertThat(response.getName()).isEqualTo("Updated Groceries");
    assertThat(response.getLimitAmount()).isEqualByComparingTo("600.00");
    assertThat(response.getYear()).isEqualTo(2026);
    assertThat(response.getMonth()).isEqualTo(5);

    ArgumentCaptor<Budget> captor = ArgumentCaptor.forClass(Budget.class);
    verify(budgetRepository).save(captor.capture());
    Budget savedBudget = captor.getValue();

    assertThat(savedBudget.getName()).isEqualTo("Updated Groceries");
    assertThat(savedBudget.getLimitAmount()).isEqualByComparingTo("600.00");
    assertThat(savedBudget.getYear()).isEqualTo(2026);
    assertThat(savedBudget.getMonth()).isEqualTo(5);
    assertThat(savedBudget.getCategory()).isEqualTo(category);
  }

  @Test
  void updateBudget_shouldThrowWhenUserNotFound() {
    UUID budgetId = UUID.randomUUID();

    UpdateBudgetRequest request = new UpdateBudgetRequest();
    request.setCategoryId(categoryId);
    request.setName("Updated Groceries");
    request.setLimitAmount(new BigDecimal("600.00"));
    request.setYear(2026);
    request.setMonth(5);

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> budgetService.updateBudget(budgetId, request, userId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found");

    verify(budgetRepository, never()).findById(any(UUID.class));
    verify(categoryRepository, never()).findById(any(UUID.class));
    verify(budgetRepository, never()).save(any(Budget.class));
  }

  @Test
  void updateBudget_shouldThrowWhenBudgetNotFound() {
    UUID budgetId = UUID.randomUUID();

    UpdateBudgetRequest request = new UpdateBudgetRequest();
    request.setCategoryId(categoryId);
    request.setName("Updated Groceries");
    request.setLimitAmount(new BigDecimal("600.00"));
    request.setYear(2026);
    request.setMonth(5);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> budgetService.updateBudget(budgetId, request, userId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Budget not found");

    verify(categoryRepository, never()).findById(any(UUID.class));
    verify(budgetRepository, never()).save(any(Budget.class));
  }

  @Test
  void updateBudget_shouldThrowForbiddenWhenBudgetNotOwnedByUser() {
    UUID budgetId = UUID.randomUUID();

    User otherUser = new User();
    otherUser.setId(UUID.randomUUID());

    Budget budget = createBudget(2026, 4);
    budget.setId(budgetId);
    budget.setUser(otherUser);

    UpdateBudgetRequest request = new UpdateBudgetRequest();
    request.setCategoryId(categoryId);
    request.setName("Updated Groceries");
    request.setLimitAmount(new BigDecimal("600.00"));
    request.setYear(2026);
    request.setMonth(5);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

    assertThatThrownBy(() -> budgetService.updateBudget(budgetId, request, userId))
        .isInstanceOf(ForbiddenException.class)
        .hasMessage("Budget does not belong to user");

    verify(categoryRepository, never()).findById(any(UUID.class));
    verify(budgetRepository, never()).save(any(Budget.class));
  }

  @Test
  void updateBudget_shouldThrowWhenCategoryNotFound() {
    UUID budgetId = UUID.randomUUID();

    Budget budget = createBudget(2026, 4);
    budget.setId(budgetId);

    UpdateBudgetRequest request = new UpdateBudgetRequest();
    request.setCategoryId(categoryId);
    request.setName("Updated Groceries");
    request.setLimitAmount(new BigDecimal("600.00"));
    request.setYear(2026);
    request.setMonth(5);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> budgetService.updateBudget(budgetId, request, userId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Category not found");

    verify(budgetRepository, never()).save(any(Budget.class));
  }

  @Test
  void updateBudget_shouldThrowForbiddenWhenCategoryNotOwnedByUser() {
    UUID budgetId = UUID.randomUUID();

    Budget budget = createBudget(2026, 4);
    budget.setId(budgetId);

    User otherUser = new User();
    otherUser.setId(UUID.randomUUID());

    Category otherUsersCategory = new Category();
    otherUsersCategory.setId(categoryId);
    otherUsersCategory.setUser(otherUser);

    UpdateBudgetRequest request = new UpdateBudgetRequest();
    request.setCategoryId(categoryId);
    request.setName("Updated Groceries");
    request.setLimitAmount(new BigDecimal("600.00"));
    request.setYear(2026);
    request.setMonth(5);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(otherUsersCategory));

    assertThatThrownBy(() -> budgetService.updateBudget(budgetId, request, userId))
        .isInstanceOf(ForbiddenException.class)
        .hasMessage("Category does not belong to user");

    verify(budgetRepository, never()).save(any(Budget.class));
  }

  @Test
  void updateBudget_shouldThrowWhenDuplicateBudgetExists() {
    UUID budgetId = UUID.randomUUID();

    Budget budget = createBudget(2026, 4);
    budget.setId(budgetId);

    UpdateBudgetRequest request = new UpdateBudgetRequest();
    request.setCategoryId(categoryId);
    request.setName("Updated Groceries");
    request.setLimitAmount(new BigDecimal("600.00"));
    request.setYear(2026);
    request.setMonth(5);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
    when(budgetRepository.existsByUserIdAndCategoryIdAndYearAndMonthAndIdNot(userId, categoryId, 2026, 5, budgetId))
        .thenReturn(true);

    assertThatThrownBy(() -> budgetService.updateBudget(budgetId, request, userId))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Budget already exists for this category and period");

    verify(budgetRepository, never()).save(any(Budget.class));
  }

  @Test
  void deleteBudget_shouldDeleteBudgetSuccessfully() {
    UUID budgetId = UUID.randomUUID();

    Budget budget = createBudget(2026, 4);
    budget.setId(budgetId);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

    budgetService.deleteBudget(budgetId, userId);

    verify(budgetRepository).delete(budget);
  }

  @Test
  void deleteBudget_shouldThrowWhenUserNotFound() {
    UUID budgetId = UUID.randomUUID();

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> budgetService.deleteBudget(budgetId, userId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found");

    verify(budgetRepository, never()).findById(any(UUID.class));
    verify(budgetRepository, never()).delete(any(Budget.class));
  }

  @Test
  void deleteBudget_shouldThrowWhenBudgetNotFound() {
    UUID budgetId = UUID.randomUUID();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> budgetService.deleteBudget(budgetId, userId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Budget not found");

    verify(budgetRepository, never()).delete(any(Budget.class));
  }

  @Test
  void deleteBudget_shouldThrowForbiddenWhenBudgetNotOwnedByUser() {
    UUID budgetId = UUID.randomUUID();

    User otherUser = new User();
    otherUser.setId(UUID.randomUUID());

    Budget budget = createBudget(2026, 4);
    budget.setId(budgetId);
    budget.setUser(otherUser);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

    assertThatThrownBy(() -> budgetService.deleteBudget(budgetId, userId))
        .isInstanceOf(ForbiddenException.class)
        .hasMessage("Budget does not belong to user");

    verify(budgetRepository, never()).delete(any(Budget.class));
  }

  private Budget createBudget(int year, int month) {
    Budget budget = new Budget();
    budget.setId(UUID.randomUUID());
    budget.setUser(user);
    budget.setCategory(category);
    budget.setName("Groceries");
    budget.setLimitAmount(new BigDecimal("500.00"));
    budget.setYear(year);
    budget.setMonth(month);
    budget.setCreatedAt(Instant.now());
    budget.setUpdatedAt(Instant.now());
    return budget;
  }
}
