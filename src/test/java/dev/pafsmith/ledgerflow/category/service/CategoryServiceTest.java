package dev.pafsmith.ledgerflow.category.service;

import dev.pafsmith.ledgerflow.category.dto.CreateCategoryRequest;
import dev.pafsmith.ledgerflow.category.entity.Category;
import dev.pafsmith.ledgerflow.category.enums.CategoryType;
import dev.pafsmith.ledgerflow.category.repository.CategoryRepository;
import dev.pafsmith.ledgerflow.common.exception.BadRequestException;
import dev.pafsmith.ledgerflow.common.exception.ResourceNotFoundException;
import dev.pafsmith.ledgerflow.user.entity.User;
import dev.pafsmith.ledgerflow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CategoryService categoryService;

  private UUID userId;
  private User user;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();

    user = new User();
    user.setId(userId);
    user.setFirstName("Paul");
    user.setLastName("Smith");
    user.setEmail("paul@test.com");
    user.setPasswordHash("hashed");
  }

  @Test
  void createCategory_shouldSaveCategorySuccessfully() {
    CreateCategoryRequest request = new CreateCategoryRequest();
    request.setUserId(userId);
    request.setName("Groceries");
    request.setType(CategoryType.EXPENSE);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(categoryRepository.existsByUserIdAndNameIgnoreCase(userId, "Groceries")).thenReturn(false);
    when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
      Category category = invocation.getArgument(0);
      category.setId(UUID.randomUUID());
      return category;
    });

    var response = categoryService.createCategory(request);

    assertThat(response).isNotNull();
    assertThat(response.getName()).isEqualTo("Groceries");
    assertThat(response.getType()).isEqualTo(CategoryType.EXPENSE);
    assertThat(response.getUserId()).isEqualTo(userId);

    verify(categoryRepository).save(any(Category.class));
  }

  @Test
  void createCategory_shouldThrowWhenUserNotFound() {
    CreateCategoryRequest request = new CreateCategoryRequest();
    request.setUserId(userId);
    request.setName("Groceries");
    request.setType(CategoryType.EXPENSE);

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.createCategory(request))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found");

    verify(categoryRepository, never()).save(any(Category.class));
  }

  @Test
  void createCategory_shouldThrowWhenDuplicateCategoryExists() {
    CreateCategoryRequest request = new CreateCategoryRequest();
    request.setUserId(userId);
    request.setName("Groceries");
    request.setType(CategoryType.EXPENSE);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(categoryRepository.existsByUserIdAndNameIgnoreCase(userId, "Groceries")).thenReturn(true);

    assertThatThrownBy(() -> categoryService.createCategory(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Category with that name already exists for this user");

    verify(categoryRepository, never()).save(any(Category.class));
  }

  @Test
  @DisplayName("GET /api/categories/{categoryId} returns 404 when category is not found")
  void getCategoryById_shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception {
    UUID categoryId = UUID.randomUUID();

    when(categoryService.getCategoryById(categoryId))
        .thenThrow(new ResourceNotFoundException("Category not found"));

    MockMvc.perform(get("/api/categories/{categoryId}", categoryId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Category not found"))
        .andExpect(jsonPath("$.path").value("/api/categories/" + categoryId));
  }
}
