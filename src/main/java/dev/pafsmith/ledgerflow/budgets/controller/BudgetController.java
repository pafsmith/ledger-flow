package dev.pafsmith.ledgerflow.budgets.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.pafsmith.ledgerflow.budgets.dto.BudgetResponse;
import dev.pafsmith.ledgerflow.budgets.dto.CreateBudgetRequest;
import dev.pafsmith.ledgerflow.budgets.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/budgets")
@Tag(name = "Budgets", description = "Operations for managing budgets")
public class BudgetController {
  private final BudgetService budgetService;

  public BudgetController(BudgetService budgetService) {
    this.budgetService = budgetService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a budget", description = "Creates a budget", responses = {
      @ApiResponse(responseCode = "201", description = "Budget created"),
      @ApiResponse(responseCode = "400", description = "Validation failed"),
      @ApiResponse(responseCode = "404", description = "Related resource not found")
  })
  public BudgetResponse createBudget(@Valid @RequestBody CreateBudgetRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    UUID userId = UUID.fromString(userDetails.getUsername());
    return budgetService.createBudget(request, userId);
  }

  @GetMapping
  @Operation(summary = "Get all budgets", description = "Gets all budgets for the user")
  public List<BudgetResponse> getBudgets(@AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(required = false) Integer year,
      @RequestParam(required = false) Integer month) {
    UUID userId = UUID.fromString(userDetails.getUsername());
    return budgetService.getBudgetsForUser(userId, year, month);
  }

}
