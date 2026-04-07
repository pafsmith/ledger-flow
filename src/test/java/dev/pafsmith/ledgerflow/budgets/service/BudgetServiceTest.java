package dev.pafsmith.ledgerflow.budgets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
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
import dev.pafsmith.ledgerflow.budgets.entity.Budget;
import dev.pafsmith.ledgerflow.budgets.repository.BudgetRepository;
import dev.pafsmith.ledgerflow.category.entity.Category;
import dev.pafsmith.ledgerflow.category.repository.CategoryRepository;
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
    request.setName("Groceries");
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
}
