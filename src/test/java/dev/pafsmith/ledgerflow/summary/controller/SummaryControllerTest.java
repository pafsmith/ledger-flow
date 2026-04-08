package dev.pafsmith.ledgerflow.summary.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.pafsmith.ledgerflow.common.BaseControllerTest;
import dev.pafsmith.ledgerflow.common.exception.BadRequestException;
import dev.pafsmith.ledgerflow.common.exception.GlobalExceptionHandler;
import dev.pafsmith.ledgerflow.summary.dto.BudgetVsActualResponse;
import dev.pafsmith.ledgerflow.summary.dto.CategorySpendResponse;
import dev.pafsmith.ledgerflow.summary.dto.MonthlySummaryResponse;
import dev.pafsmith.ledgerflow.summary.service.SummaryService;

@WebMvcTest(SummaryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WithMockUser(username = "11111111-1111-1111-1111-111111111111")
class SummaryControllerTest extends BaseControllerTest {

  private static final UUID AUTH_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private SummaryService summaryService;

  @Test
  @DisplayName("GET /api/summary/monthly returns 200 when request is valid")
  void getMonthlySummary_shouldReturnOk() throws Exception {
    CategorySpendResponse categorySpend = new CategorySpendResponse();
    categorySpend.setCategoryId(UUID.randomUUID());
    categorySpend.setCategoryName("Groceries");
    categorySpend.setAmount(new BigDecimal("500.00"));

    BudgetVsActualResponse budgetVsActual = new BudgetVsActualResponse();
    budgetVsActual.setBudgetId(UUID.randomUUID());
    budgetVsActual.setCategoryId(categorySpend.getCategoryId());
    budgetVsActual.setCategoryName("Groceries");
    budgetVsActual.setBudgetName("Groceries Budget");
    budgetVsActual.setBudgetLimit(new BigDecimal("400.00"));
    budgetVsActual.setActualSpent(new BigDecimal("500.00"));
    budgetVsActual.setRemaining(new BigDecimal("-100.00"));
    budgetVsActual.setOverBudget(true);

    MonthlySummaryResponse response = new MonthlySummaryResponse();
    response.setMonth(4);
    response.setYear(2026);
    response.setTotalIncome(new BigDecimal("3000.00"));
    response.setTotalExpenses(new BigDecimal("700.00"));
    response.setNet(new BigDecimal("2300.00"));
    response.setSpendByCategory(List.of(categorySpend));
    response.setBudgetVsActual(List.of(budgetVsActual));

    when(summaryService.getMonthlySummary(eq(AUTH_USER_ID), eq(2026), eq(4))).thenReturn(response);

    mockMvc.perform(get("/api/summary/monthly")
        .param("month", "4")
        .param("year", "2026"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.month").value(4))
        .andExpect(jsonPath("$.year").value(2026))
        .andExpect(jsonPath("$.totalIncome").value(3000.00))
        .andExpect(jsonPath("$.totalExpenses").value(700.00))
        .andExpect(jsonPath("$.net").value(2300.00))
        .andExpect(jsonPath("$.spendByCategory[0].categoryName").value("Groceries"))
        .andExpect(jsonPath("$.budgetVsActual[0].overBudget").value(true));
  }

  @Test
  @DisplayName("GET /api/summary/monthly returns 400 when month is missing")
  void getMonthlySummary_shouldReturnBadRequest_whenMonthIsMissing() throws Exception {
    mockMvc.perform(get("/api/summary/monthly")
        .param("year", "2026"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Required request parameter 'month' is missing"))
        .andExpect(jsonPath("$.path").value("/api/summary/monthly"));
  }

  @Test
  @DisplayName("GET /api/summary/monthly returns 400 when month is not numeric")
  void getMonthlySummary_shouldReturnBadRequest_whenMonthIsNotNumeric() throws Exception {
    mockMvc.perform(get("/api/summary/monthly")
        .param("month", "abc")
        .param("year", "2026"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Invalid value for parameter 'month'"))
        .andExpect(jsonPath("$.path").value("/api/summary/monthly"));
  }

  @Test
  @DisplayName("GET /api/summary/monthly returns 400 when filters are invalid")
  void getMonthlySummary_shouldReturnBadRequest_whenFiltersAreInvalid() throws Exception {
    when(summaryService.getMonthlySummary(eq(AUTH_USER_ID), eq(2026), eq(13)))
        .thenThrow(new BadRequestException("Month must be between 1 and 12"));

    mockMvc.perform(get("/api/summary/monthly")
        .param("month", "13")
        .param("year", "2026"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Month must be between 1 and 12"))
        .andExpect(jsonPath("$.path").value("/api/summary/monthly"));
  }
}
