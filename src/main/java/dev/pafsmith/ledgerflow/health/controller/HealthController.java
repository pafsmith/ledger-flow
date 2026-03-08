package dev.pafsmith.ledgerflow.health.controller;

import dev.pafsmith.ledgerflow.health.dto.HealthResponse;
import dev.pafsmith.ledgerflow.health.service.HealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/api/health")
    public HealthResponse health() {
        return healthService.getHealth();
    }
}
