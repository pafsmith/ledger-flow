package dev.pafsmith.ledgerflow.category.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dev.pafsmith.ledgerflow.category.dto.CategoryResponse;
import dev.pafsmith.ledgerflow.category.dto.CreateCategoryRequest;
import dev.pafsmith.ledgerflow.category.enums.CategoryType;
import dev.pafsmith.ledgerflow.category.service.CategoryService;
import dev.pafsmith.ledgerflow.common.BaseControllerTest;
import dev.pafsmith.ledgerflow.common.exception.GlobalExceptionHandler;
import dev.pafsmith.ledgerflow.common.exception.ResourceNotFoundException;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WithMockUser(username = "11111111-1111-1111-1111-111111111111")
class CategoryControllerTest extends BaseControllerTest {

  private static final UUID AUTH_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule());

  @MockitoBean
  private CategoryService categoryService;

  @Test
  @DisplayName("POST /api/categories returns 201 when request is valid")
  void createCategory_shouldReturnCreated() throws Exception {
    UUID categoryId = UUID.randomUUID();

    CreateCategoryRequest request = new CreateCategoryRequest();
    request.setName("Groceries");
    request.setType(CategoryType.EXPENSE);

    CategoryResponse response = new CategoryResponse();
    response.setId(categoryId);
    response.setUserId(AUTH_USER_ID);
    response.setName("Groceries");
    response.setType(CategoryType.EXPENSE);
    response.setSystemDefined(false);
    response.setCreatedAt(Instant.now());
    response.setUpdatedAt(Instant.now());

    when(categoryService.createCategory(eq(AUTH_USER_ID), any(CreateCategoryRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/categories")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(categoryId.toString()))
        .andExpect(jsonPath("$.name").value("Groceries"))
        .andExpect(jsonPath("$.type").value("EXPENSE"));
  }

  @Test
  @DisplayName("POST /api/categories returns 400 when request is invalid")
  void createCategory_shouldReturnBadRequest_whenValidationFails() throws Exception {
    String invalidJson = """
        {
          "name": "",
          "type": null
        }
        """;

    mockMvc.perform(post("/api/categories")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.validationErrors.name").value("Category name is required"))
        .andExpect(jsonPath("$.validationErrors.type").value("Category type is required"));
  }

  @Test
  @DisplayName("GET /api/categories/{categoryId} returns category when found")
  void getCategoryById_shouldReturnCategory() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();

    CategoryResponse response = new CategoryResponse();
    response.setId(categoryId);
    response.setUserId(userId);
    response.setName("Groceries");
    response.setType(CategoryType.EXPENSE);
    response.setSystemDefined(false);
    response.setCreatedAt(Instant.now());
    response.setUpdatedAt(Instant.now());

    when(categoryService.getCategoryById(AUTH_USER_ID, categoryId)).thenReturn(response);

    mockMvc.perform(get("/api/categories/{categoryId}", categoryId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(categoryId.toString()))
        .andExpect(jsonPath("$.userId").value(userId.toString()))
        .andExpect(jsonPath("$.name").value("Groceries"))
        .andExpect(jsonPath("$.type").value("EXPENSE"));
  }

  @Test
  @DisplayName("GET /api/categories returns categories for authenticated user")
  void getCategoriesForUser_shouldReturnCategories() throws Exception {
    CategoryResponse groceries = new CategoryResponse();
    groceries.setId(UUID.randomUUID());
    groceries.setUserId(AUTH_USER_ID);
    groceries.setName("Groceries");
    groceries.setType(CategoryType.EXPENSE);
    groceries.setSystemDefined(false);
    groceries.setCreatedAt(Instant.now());
    groceries.setUpdatedAt(Instant.now());

    CategoryResponse salary = new CategoryResponse();
    salary.setId(UUID.randomUUID());
    salary.setUserId(AUTH_USER_ID);
    salary.setName("Salary");
    salary.setType(CategoryType.INCOME);
    salary.setSystemDefined(false);
    salary.setCreatedAt(Instant.now());
    salary.setUpdatedAt(Instant.now());

    when(categoryService.getCategoriesForUser(AUTH_USER_ID)).thenReturn(List.of(groceries, salary));

    mockMvc.perform(get("/api/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Groceries"))
        .andExpect(jsonPath("$[0].type").value("EXPENSE"))
        .andExpect(jsonPath("$[1].name").value("Salary"))
        .andExpect(jsonPath("$[1].type").value("INCOME"));
  }

  @Test
  @DisplayName("GET /api/categories/type/{type} returns filtered categories for authenticated user")
  void getCategoriesForUserByType_shouldReturnFilteredCategories() throws Exception {
    CategoryResponse groceries = new CategoryResponse();
    groceries.setId(UUID.randomUUID());
    groceries.setUserId(AUTH_USER_ID);
    groceries.setName("Groceries");
    groceries.setType(CategoryType.EXPENSE);
    groceries.setSystemDefined(false);
    groceries.setCreatedAt(Instant.now());
    groceries.setUpdatedAt(Instant.now());

    CategoryResponse transport = new CategoryResponse();
    transport.setId(UUID.randomUUID());
    transport.setUserId(AUTH_USER_ID);
    transport.setName("Transport");
    transport.setType(CategoryType.EXPENSE);
    transport.setSystemDefined(false);
    transport.setCreatedAt(Instant.now());
    transport.setUpdatedAt(Instant.now());

    when(categoryService.getCategoriesForUserByType(AUTH_USER_ID, CategoryType.EXPENSE))
        .thenReturn(List.of(groceries, transport));

    mockMvc.perform(get("/api/categories/type/{type}", CategoryType.EXPENSE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Groceries"))
        .andExpect(jsonPath("$[0].type").value("EXPENSE"))
        .andExpect(jsonPath("$[1].name").value("Transport"))
        .andExpect(jsonPath("$[1].type").value("EXPENSE"));
  }

  @Test
  @DisplayName("GET /api/categories/{categoryId} returns 404 when category is not found")
  void getCategoryById_shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception {
    UUID categoryId = UUID.randomUUID();

    when(categoryService.getCategoryById(AUTH_USER_ID, categoryId))
        .thenThrow(new ResourceNotFoundException("Category not found"));

    mockMvc.perform(get("/api/categories/{categoryId}", categoryId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Category not found"))
        .andExpect(jsonPath("$.path").value("/api/categories/" + categoryId));
  }
}
