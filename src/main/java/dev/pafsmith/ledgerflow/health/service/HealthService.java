package dev.pafsmith.ledgerflow.health.service;
import dev.pafsmith.ledgerflow.health.dto.HealthResponse;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;

@Service
public class HealthService {

    @Value("${app.version}")
    private String version;

    public HealthResponse getHealth() {
        return new HealthResponse(
                "UP",
                Instant.now(),
                "ledgerflow",
                version
        );
    }
}