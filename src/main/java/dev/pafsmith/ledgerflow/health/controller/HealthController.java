package dev.pafsmith.ledgerflow.health.controller;

import dev.pafsmith.ledgerflow.health.dto.HealthResponse;
import dev.pafsmith.ledgerflow.health.service.HealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Operations for checking application health")
public class HealthController {

  private final HealthService healthService;

  public HealthController(HealthService healthService) {
    this.healthService = healthService;
  }

  @GetMapping("/api/health")
  @Operation(summary = "Check application health", description = "Returns current health of ledgerflow api")
  public HealthResponse health() {
    return healthService.getHealth();
  }
}
