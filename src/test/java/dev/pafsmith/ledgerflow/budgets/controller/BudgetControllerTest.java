package dev.pafsmith.ledgerflow.budgets.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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

import dev.pafsmith.ledgerflow.budgets.dto.BudgetResponse;
import dev.pafsmith.ledgerflow.budgets.dto.CreateBudgetRequest;
import dev.pafsmith.ledgerflow.budgets.dto.UpdateBudgetRequest;
import dev.pafsmith.ledgerflow.budgets.service.BudgetService;
import dev.pafsmith.ledgerflow.common.BaseControllerTest;
import dev.pafsmith.ledgerflow.common.exception.BadRequestException;
import dev.pafsmith.ledgerflow.common.exception.ForbiddenException;
import dev.pafsmith.ledgerflow.common.exception.GlobalExceptionHandler;
import dev.pafsmith.ledgerflow.common.exception.ResourceNotFoundException;

@WebMvcTest(BudgetController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WithMockUser(username = "11111111-1111-1111-1111-111111111111")
class BudgetControllerTest extends BaseControllerTest {

  private static final UUID AUTH_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule());

  @MockitoBean
  private BudgetService budgetService;

  @Test
  @DisplayName("POST /api/budgets returns 201 when request is valid")
  void createBudget_shouldReturnCreated() throws Exception {
    UUID budgetId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();

    CreateBudgetRequest request = new CreateBudgetRequest();
    request.setCategoryId(categoryId);
    request.setName("Groceries");
    request.setLimitAmount(new BigDecimal("500.00"));
    request.setYear(2026);
    request.setMonth(4);

    BudgetResponse response = new BudgetResponse();
    response.setId(budgetId);
    response.setUserId(AUTH_USER_ID);
    response.setCategoryId(categoryId);
    response.setName("Groceries");
    response.setYear(2026);
    response.setMonth(4);
    response.setCreatedAt(Instant.now());
    response.setUpdatedAt(Instant.now());

    when(budgetService.createBudget(any(CreateBudgetRequest.class), eq(AUTH_USER_ID))).thenReturn(response);

    mockMvc.perform(post("/api/budgets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(budgetId.toString()))
        .andExpect(jsonPath("$.userId").value(AUTH_USER_ID.toString()))
        .andExpect(jsonPath("$.categoryId").value(categoryId.toString()))
        .andExpect(jsonPath("$.name").value("Groceries"))
        .andExpect(jsonPath("$.year").value(2026))
        .andExpect(jsonPath("$.month").value(4));
  }

  @Test
  @DisplayName("POST /api/budgets returns 400 when request is invalid")
  void createBudget_shouldReturnBadRequest_whenValidationFails() throws Exception {
    String invalidJson = "{}";

    mockMvc.perform(post("/api/budgets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.validationErrors.categoryId").exists())
        .andExpect(jsonPath("$.validationErrors.name").exists())
        .andExpect(jsonPath("$.validationErrors.limitAmount").exists())
        .andExpect(jsonPath("$.validationErrors.year").exists())
        .andExpect(jsonPath("$.validationErrors.month").exists());
  }

  @Test
  @DisplayName("POST /api/budgets returns 400 when request has invalid field values")
  void createBudget_shouldReturnBadRequest_whenConstraintValidationFails() throws Exception {
    CreateBudgetRequest request = new CreateBudgetRequest();
    request.setCategoryId(UUID.randomUUID());
    request.setName(" ");
    request.setLimitAmount(new BigDecimal("-1.00"));
    request.setYear(2026);
    request.setMonth(13);

    mockMvc.perform(post("/api/budgets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.validationErrors.name").exists())
        .andExpect(jsonPath("$.validationErrors.limitAmount").exists())
        .andExpect(jsonPath("$.validationErrors.month").exists());
  }

  @Test
  @DisplayName("GET /api/budgets returns 200 with budget list")
  void getBudgets_shouldReturnOk() throws Exception {
    UUID budgetId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();

    BudgetResponse response = new BudgetResponse();
    response.setId(budgetId);
    response.setUserId(AUTH_USER_ID);
    response.setCategoryId(categoryId);
    response.setName("Groceries");
    response.setLimitAmount(new BigDecimal("500.00"));
    response.setYear(2026);
    response.setMonth(4);
    response.setCreatedAt(Instant.now());
    response.setUpdatedAt(Instant.now());

    when(budgetService.getBudgetsForUser(AUTH_USER_ID, 2026, 4)).thenReturn(List.of(response));

    mockMvc.perform(get("/api/budgets")
        .param("year", "2026")
        .param("month", "4"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(budgetId.toString()))
        .andExpect(jsonPath("$[0].userId").value(AUTH_USER_ID.toString()))
        .andExpect(jsonPath("$[0].categoryId").value(categoryId.toString()))
        .andExpect(jsonPath("$[0].name").value("Groceries"))
        .andExpect(jsonPath("$[0].year").value(2026))
        .andExpect(jsonPath("$[0].month").value(4));
  }

  @Test
  @DisplayName("GET /api/budgets/{budgetId} returns 200 when budget exists")
  void getBudgetById_shouldReturnOk() throws Exception {
    UUID budgetId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();

    BudgetResponse response = new BudgetResponse();
    response.setId(budgetId);
    response.setUserId(AUTH_USER_ID);
    response.setCategoryId(categoryId);
    response.setName("Groceries");
    response.setLimitAmount(new BigDecimal("500.00"));
    response.setYear(2026);
    response.setMonth(4);
    response.setCreatedAt(Instant.now());
    response.setUpdatedAt(Instant.now());

    when(budgetService.getBudgetById(budgetId, AUTH_USER_ID)).thenReturn(response);

    mockMvc.perform(get("/api/budgets/{budgetId}", budgetId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(budgetId.toString()))
        .andExpect(jsonPath("$.userId").value(AUTH_USER_ID.toString()))
        .andExpect(jsonPath("$.categoryId").value(categoryId.toString()))
        .andExpect(jsonPath("$.name").value("Groceries"))
        .andExpect(jsonPath("$.year").value(2026))
        .andExpect(jsonPath("$.month").value(4));
  }

  @Test
  @DisplayName("GET /api/budgets/{budgetId} returns 404 when budget does not exist")
  void getBudgetById_shouldReturnNotFound_whenBudgetDoesNotExist() throws Exception {
    UUID budgetId = UUID.randomUUID();

    when(budgetService.getBudgetById(budgetId, AUTH_USER_ID))
        .thenThrow(new ResourceNotFoundException("Budget not found"));

    mockMvc.perform(get("/api/budgets/{budgetId}", budgetId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Budget not found"))
        .andExpect(jsonPath("$.path").value("/api/budgets/" + budgetId));
  }

  @Test
  @DisplayName("GET /api/budgets/{budgetId} returns 403 when budget belongs to another user")
  void getBudgetById_shouldReturnForbidden_whenBudgetDoesNotBelongToUser() throws Exception {
    UUID budgetId = UUID.randomUUID();

    when(budgetService.getBudgetById(budgetId, AUTH_USER_ID))
        .thenThrow(new ForbiddenException("Budget does not belong to user"));

    mockMvc.perform(get("/api/budgets/{budgetId}", budgetId))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.error").value("Forbidden"))
        .andExpect(jsonPath("$.message").value("Budget does not belong to user"))
        .andExpect(jsonPath("$.path").value("/api/budgets/" + budgetId));
  }

  @Test
  @DisplayName("GET /api/budgets returns 400 when filters are invalid")
  void getBudgets_shouldReturnBadRequest_whenFiltersAreInvalid() throws Exception {
    when(budgetService.getBudgetsForUser(AUTH_USER_ID, 2026, 13))
        .thenThrow(new BadRequestException("Month must be between 1 and 12"));

    mockMvc.perform(get("/api/budgets")
        .param("year", "2026")
        .param("month", "13"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Month must be between 1 and 12"))
        .andExpect(jsonPath("$.path").value("/api/budgets"));
  }

  @Test
  @DisplayName("GET unknown endpoint returns 404")
  void unknownEndpoint_shouldReturnNotFound() throws Exception {
    mockMvc.perform(get("/api/not-a-route"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Resource not found"))
        .andExpect(jsonPath("$.path").value("/api/not-a-route"));
  }

  @Test
  @DisplayName("POST /api/budgets returns 404 when category is not found")
  void createBudget_shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception {
    UUID categoryId = UUID.randomUUID();

    CreateBudgetRequest request = new CreateBudgetRequest();
    request.setCategoryId(categoryId);
    request.setName("Groceries");
    request.setLimitAmount(new BigDecimal("500.00"));
    request.setYear(2026);
    request.setMonth(4);

    when(budgetService.createBudget(any(CreateBudgetRequest.class), eq(AUTH_USER_ID)))
        .thenThrow(new ResourceNotFoundException("Category not found"));

    mockMvc.perform(post("/api/budgets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Category not found"))
        .andExpect(jsonPath("$.path").value("/api/budgets"));
  }

  @Test
  @DisplayName("PUT /api/budgets/{budgetId} returns 200 when request is valid")
  void updateBudget_shouldReturnOk() throws Exception {
    UUID budgetId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();

    UpdateBudgetRequest request = new UpdateBudgetRequest();
    request.setCategoryId(categoryId);
    request.setName("Updated Groceries");
    request.setLimitAmount(new BigDecimal("600.00"));
    request.setYear(2026);
    request.setMonth(5);

    BudgetResponse response = new BudgetResponse();
    response.setId(budgetId);
    response.setUserId(AUTH_USER_ID);
    response.setCategoryId(categoryId);
    response.setName("Updated Groceries");
    response.setLimitAmount(new BigDecimal("600.00"));
    response.setYear(2026);
    response.setMonth(5);
    response.setCreatedAt(Instant.now());
    response.setUpdatedAt(Instant.now());

    when(budgetService.updateBudget(eq(budgetId), any(UpdateBudgetRequest.class), eq(AUTH_USER_ID)))
        .thenReturn(response);

    mockMvc.perform(put("/api/budgets/{budgetId}", budgetId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(budgetId.toString()))
        .andExpect(jsonPath("$.userId").value(AUTH_USER_ID.toString()))
        .andExpect(jsonPath("$.categoryId").value(categoryId.toString()))
        .andExpect(jsonPath("$.name").value("Updated Groceries"))
        .andExpect(jsonPath("$.year").value(2026))
        .andExpect(jsonPath("$.month").value(5));
  }

  @Test
  @DisplayName("PUT /api/budgets/{budgetId} returns 400 when request is invalid")
  void updateBudget_shouldReturnBadRequest_whenValidationFails() throws Exception {
    UUID budgetId = UUID.randomUUID();
    String invalidJson = "{}";

    mockMvc.perform(put("/api/budgets/{budgetId}", budgetId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.validationErrors.categoryId").exists())
        .andExpect(jsonPath("$.validationErrors.name").exists())
        .andExpect(jsonPath("$.validationErrors.limitAmount").exists())
        .andExpect(jsonPath("$.validationErrors.year").exists())
        .andExpect(jsonPath("$.validationErrors.month").exists());
  }

  @Test
  @DisplayName("PUT /api/budgets/{budgetId} returns 404 when budget does not exist")
  void updateBudget_shouldReturnNotFound_whenBudgetDoesNotExist() throws Exception {
    UUID budgetId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();

    UpdateBudgetRequest request = new UpdateBudgetRequest();
    request.setCategoryId(categoryId);
    request.setName("Updated Groceries");
    request.setLimitAmount(new BigDecimal("600.00"));
    request.setYear(2026);
    request.setMonth(5);

    when(budgetService.updateBudget(eq(budgetId), any(UpdateBudgetRequest.class), eq(AUTH_USER_ID)))
        .thenThrow(new ResourceNotFoundException("Budget not found"));

    mockMvc.perform(put("/api/budgets/{budgetId}", budgetId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Budget not found"))
        .andExpect(jsonPath("$.path").value("/api/budgets/" + budgetId));
  }

  @Test
  @DisplayName("DELETE /api/budgets/{budgetId} returns 204 when budget exists")
  void deleteBudget_shouldReturnNoContent() throws Exception {
    UUID budgetId = UUID.randomUUID();

    doNothing().when(budgetService).deleteBudget(budgetId, AUTH_USER_ID);

    mockMvc.perform(delete("/api/budgets/{budgetId}", budgetId))
        .andExpect(status().isNoContent());

    verify(budgetService).deleteBudget(budgetId, AUTH_USER_ID);
  }

  @Test
  @DisplayName("DELETE /api/budgets/{budgetId} returns 404 when budget does not exist")
  void deleteBudget_shouldReturnNotFound_whenBudgetDoesNotExist() throws Exception {
    UUID budgetId = UUID.randomUUID();

    doThrow(new ResourceNotFoundException("Budget not found"))
        .when(budgetService)
        .deleteBudget(budgetId, AUTH_USER_ID);

    mockMvc.perform(delete("/api/budgets/{budgetId}", budgetId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Budget not found"))
        .andExpect(jsonPath("$.path").value("/api/budgets/" + budgetId));
  }

  @Test
  @DisplayName("DELETE /api/budgets/{budgetId} returns 403 when budget belongs to another user")
  void deleteBudget_shouldReturnForbidden_whenBudgetDoesNotBelongToUser() throws Exception {
    UUID budgetId = UUID.randomUUID();

    doThrow(new ForbiddenException("Budget does not belong to user"))
        .when(budgetService)
        .deleteBudget(budgetId, AUTH_USER_ID);

    mockMvc.perform(delete("/api/budgets/{budgetId}", budgetId))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.error").value("Forbidden"))
        .andExpect(jsonPath("$.message").value("Budget does not belong to user"))
        .andExpect(jsonPath("$.path").value("/api/budgets/" + budgetId));
  }
}
