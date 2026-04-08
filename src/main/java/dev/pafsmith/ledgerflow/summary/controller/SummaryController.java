package dev.pafsmith.ledgerflow.summary.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.pafsmith.ledgerflow.summary.dto.MonthlySummaryResponse;
import dev.pafsmith.ledgerflow.summary.service.SummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/summary")
@Tag(name = "Summary", description = "Operations for reporting and analytics")
@SecurityRequirement(name = "bearerAuth")
public class SummaryController {
  private final SummaryService summaryService;

  public SummaryController(SummaryService summaryService) {
    this.summaryService = summaryService;
  }

  @GetMapping("/monthly")
  @Operation(summary = "Get monthly summary", description = "Gets income, expenses, net, spend by category, and budget vs actual for a month", responses = {
      @ApiResponse(responseCode = "200", description = "Summary returned"),
      @ApiResponse(responseCode = "400", description = "Invalid parameters"),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public MonthlySummaryResponse getMonthlySummary(
      @Parameter(description = "Month number between 1 and 12") @RequestParam Integer month,
      @Parameter(description = "Year greater than 0") @RequestParam Integer year,
      @AuthenticationPrincipal UserDetails userDetails) {
    UUID userId = UUID.fromString(userDetails.getUsername());
    return summaryService.getMonthlySummary(userId, year, month);
  }
}
