package dev.pafsmith.ledgerflow.budgets.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import dev.pafsmith.ledgerflow.budgets.dto.BudgetResponse;
import dev.pafsmith.ledgerflow.budgets.dto.CreateBudgetRequest;
import dev.pafsmith.ledgerflow.budgets.dto.UpdateBudgetRequest;
import dev.pafsmith.ledgerflow.budgets.service.BudgetService;

@RestController
@RequestMapping("/api/budgets")
@Tag(name = "Budgets", description = "Operations for managing budgets")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {
  private final BudgetService budgetService;

  public BudgetController(BudgetService budgetService) {
    this.budgetService = budgetService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a budget", description = "Creates a budget", responses = {
      @ApiResponse(responseCode = "201", description = "Budget created"),
      @ApiResponse(responseCode = "400", description = "Validation failed or budget already exists"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "Related resource not found")
  })
  public BudgetResponse createBudget(@Valid @RequestBody CreateBudgetRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    UUID userId = UUID.fromString(userDetails.getUsername());
    return budgetService.createBudget(request, userId);
  }

  @GetMapping({ "", "/" })
  @Operation(summary = "Get all budgets", description = "Gets all budgets for the user", responses = {
      @ApiResponse(responseCode = "200", description = "Budgets returned"),
      @ApiResponse(responseCode = "400", description = "Invalid filter values"),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public List<BudgetResponse> getBudgets(@AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Budget year filter (must be greater than 0)") @RequestParam(required = false) Integer year,
      @Parameter(description = "Budget month filter (1-12)") @RequestParam(required = false) Integer month) {
    UUID userId = UUID.fromString(userDetails.getUsername());
    return budgetService.getBudgetsForUser(userId, year, month);
  }

  @PutMapping("/{budgetId}")
  @Operation(summary = "Update a budget", description = "Updates a budget by id", responses = {
      @ApiResponse(responseCode = "200", description = "Budget updated"),
      @ApiResponse(responseCode = "400", description = "Validation failed"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  public BudgetResponse updateBudget(@PathVariable UUID budgetId,
      @Valid @RequestBody UpdateBudgetRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    UUID userId = UUID.fromString(userDetails.getUsername());
    return budgetService.updateBudget(budgetId, request, userId);
  }

  @DeleteMapping("/{budgetId}")
  @Operation(summary = "Delete a budget", description = "Deletes a budget by id", responses = {
      @ApiResponse(responseCode = "204", description = "Budget deleted"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteBudget(@PathVariable UUID budgetId, @AuthenticationPrincipal UserDetails userDetails) {
    UUID userId = UUID.fromString(userDetails.getUsername());
    budgetService.deleteBudget(budgetId, userId);
  }
}
