package dev.pafsmith.ledgerflow.transaction.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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

import dev.pafsmith.ledgerflow.common.BaseControllerTest;
import dev.pafsmith.ledgerflow.common.exception.GlobalExceptionHandler;
import dev.pafsmith.ledgerflow.common.exception.ResourceNotFoundException;
import dev.pafsmith.ledgerflow.transaction.dto.CreateTransactionRequest;
import dev.pafsmith.ledgerflow.transaction.dto.PagedTransactionResponse;
import dev.pafsmith.ledgerflow.transaction.dto.TransactionFilterRequest;
import dev.pafsmith.ledgerflow.transaction.dto.TransactionResponse;
import dev.pafsmith.ledgerflow.transaction.dto.UpdateTransactionRequest;
import dev.pafsmith.ledgerflow.transaction.enums.TransactionType;
import dev.pafsmith.ledgerflow.transaction.service.TransactionService;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WithMockUser(username = "11111111-1111-1111-1111-111111111111")
class TransactionControllerTest extends BaseControllerTest {

  private static final String AUTH_USER_ID = "11111111-1111-1111-1111-111111111111";

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule());

  @MockitoBean
  private TransactionService transactionService;

  @Test
  @DisplayName("POST /api/transactions returns 201 when request is valid")
  void createTransaction_shouldReturnCreated() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();
    UUID transactionId = UUID.randomUUID();

    CreateTransactionRequest request = new CreateTransactionRequest();
    request.setAccountId(accountId);
    request.setCategoryId(categoryId);
    request.setDescription("Tesco shop");
    request.setAmount(new BigDecimal("45.50"));
    request.setType(TransactionType.EXPENSE);
    request.setTransactionDate(LocalDate.of(2026, 3, 10));
    request.setMerchant("Tesco");

    TransactionResponse response = new TransactionResponse();
    response.setId(transactionId);
    response.setUserId(userId);
    response.setAccountId(accountId);
    response.setCategoryId(categoryId);
    response.setDescription("Tesco shop");
    response.setAmount(new BigDecimal("45.50"));
    response.setType(TransactionType.EXPENSE);
    response.setTransactionDate(LocalDate.of(2026, 3, 10));
    response.setMerchant("Tesco");
    response.setCreatedAt(Instant.now());
    response.setUpdatedAt(Instant.now());

    when(transactionService.createTransaction(any(CreateTransactionRequest.class), any(String.class)))
        .thenReturn(response);

    mockMvc.perform(post("/api/transactions")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(transactionId.toString()))
        .andExpect(jsonPath("$.description").value("Tesco shop"))
        .andExpect(jsonPath("$.type").value("EXPENSE"));
  }

  @Test
  @DisplayName("POST /api/transactions returns 400 when request is invalid")
  void createTransaction_shouldReturnBadRequest_whenValidationFails() throws Exception {
    String invalidJson = """
        {
          "description": "",
          "amount": 0,
          "type": null,
          "transactionDate": null
        }
        """;

    mockMvc.perform(post("/api/transactions")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.validationErrors.accountId").value("Account id is required"))
        .andExpect(jsonPath("$.validationErrors.description").value("Description is required"));
  }

  @Test
  @DisplayName("DELETE /api/transactions/{transactionId} returns 204 when transaction exists")
  void deleteTransaction_shouldReturnNoContent() throws Exception {
    UUID transactionId = UUID.randomUUID();

    doNothing().when(transactionService).deleteTransaction(transactionId, AUTH_USER_ID);

    mockMvc.perform(delete("/api/transactions/{transactionId}", transactionId))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /api/transactions/{transactionId} returns 404 when transaction does not exist")
  void deleteTransaction_shouldReturnNotFound_whenTransactionDoesNotExist() throws Exception {
    UUID transactionId = UUID.randomUUID();

    doThrow(new ResourceNotFoundException("Transaction not found"))
        .when(transactionService)
        .deleteTransaction(transactionId, AUTH_USER_ID);

    mockMvc.perform(delete("/api/transactions/{transactionId}", transactionId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Transaction not found"))
        .andExpect(jsonPath("$.path").value("/api/transactions/" + transactionId));
  }

  @Test
  @DisplayName("PUT /api/transactions/{transactionId} returns 200 when request is valid")
  void updateTransaction_shouldReturnOk() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();
    UUID transactionId = UUID.randomUUID();

    UpdateTransactionRequest request = new UpdateTransactionRequest();
    // request.setUserId(userId);
    request.setAccountId(accountId);
    request.setCategoryId(categoryId);
    request.setDescription("Updated Tesco shop");
    request.setAmount(new BigDecimal("55.00"));
    request.setType(TransactionType.EXPENSE);
    request.setTransactionDate(LocalDate.of(2026, 3, 10));
    request.setMerchant("Tesco");

    TransactionResponse response = new TransactionResponse();
    response.setId(transactionId);
    response.setUserId(userId);
    response.setAccountId(accountId);
    response.setCategoryId(categoryId);
    response.setDescription("Updated Tesco shop");
    response.setAmount(new BigDecimal("55.00"));
    response.setType(TransactionType.EXPENSE);
    response.setTransactionDate(LocalDate.of(2026, 3, 10));
    response.setMerchant("Tesco");
    response.setCreatedAt(Instant.now());
    response.setUpdatedAt(Instant.now());

    when(transactionService.updateTransaction(any(UUID.class), any(UpdateTransactionRequest.class), any(String.class)))
        .thenReturn(response);

    mockMvc.perform(put("/api/transactions/{transactionId}", transactionId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(transactionId.toString()))
        .andExpect(jsonPath("$.description").value("Updated Tesco shop"))
        .andExpect(jsonPath("$.type").value("EXPENSE"));
  }

  @Test
  @DisplayName("PUT /api/transactions/{transactionId} returns 400 when request is invalid")
  void updateTransaction_shouldReturnBadRequest_whenValidationFails() throws Exception {
    UUID transactionId = UUID.randomUUID();

    String invalidJson = """
        {
          "description": "",
          "amount": 0,
          "type": null,
          "transactionDate": null
        }
        """;

    mockMvc.perform(put("/api/transactions/{transactionId}", transactionId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        // .andExpect(jsonPath("$.validationErrors.userId").value("User id is
        // required"))
        .andExpect(jsonPath("$.validationErrors.accountId").value("Account id is required"))
        .andExpect(jsonPath("$.validationErrors.description").value("Description is required"));
  }

  @Test
  @DisplayName("GET /api/transactions returns paged transactions for authenticated user")
  void getTransactions_shouldReturnPagedTransactions() throws Exception {
    PagedTransactionResponse response = new PagedTransactionResponse();

    TransactionResponse transaction = new TransactionResponse();
    transaction.setId(UUID.randomUUID());
    transaction.setDescription("Tesco shop");
    transaction.setAmount(new BigDecimal("45.50"));
    transaction.setType(TransactionType.EXPENSE);
    transaction.setTransactionDate(LocalDate.of(2026, 3, 10));

    response.setContent(java.util.List.of(transaction));
    response.setPage(0);
    response.setSize(10);
    response.setTotalElements(1);
    response.setTotalPages(1);
    response.setFirst(true);
    response.setLast(true);

    when(transactionService.getTransactions(
        anyString(),
        any(TransactionFilterRequest.class),
        eq(0),
        eq(10),
        eq("transactionDate"),
        eq("desc"))).thenReturn(response);

    mockMvc.perform(get("/api/transactions")
        .param("page", "0")
        .param("size", "10")
        .param("sortBy", "transactionDate")
        .param("direction", "desc"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].description").value("Tesco shop"))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.first").value(true));
  }
}
