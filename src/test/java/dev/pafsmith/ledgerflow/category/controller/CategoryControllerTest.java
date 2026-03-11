package dev.pafsmith.ledgerflow.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.pafsmith.ledgerflow.category.dto.CategoryResponse;
import dev.pafsmith.ledgerflow.category.dto.CreateCategoryRequest;
import dev.pafsmith.ledgerflow.category.enums.CategoryType;
import dev.pafsmith.ledgerflow.category.service.CategoryService;
import dev.pafsmith.ledgerflow.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CategoryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule());

  @MockitoBean
  private CategoryService categoryService;

  @Test
  @DisplayName("POST /api/categories returns 201 when request is valid")
  void createCategory_shouldReturnCreated() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();

    CreateCategoryRequest request = new CreateCategoryRequest();
    request.setUserId(userId);
    request.setName("Groceries");
    request.setType(CategoryType.EXPENSE);

    CategoryResponse response = new CategoryResponse();
    response.setId(categoryId);
    response.setUserId(userId);
    response.setName("Groceries");
    response.setType(CategoryType.EXPENSE);
    response.setSystemDefined(false);
    response.setCreatedAt(Instant.now());
    response.setUpdatedAt(Instant.now());

    when(categoryService.createCategory(any(CreateCategoryRequest.class))).thenReturn(response);

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
          "userId": null,
          "name": "",
          "type": null
        }
        """;

    mockMvc.perform(post("/api/categories")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.validationErrors.userId").value("User id is required"))
        .andExpect(jsonPath("$.validationErrors.name").value("Category name is required"))
        .andExpect(jsonPath("$.validationErrors.type").value("Category type is required"));
  }
}
