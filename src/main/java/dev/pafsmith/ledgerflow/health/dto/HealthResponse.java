package dev.pafsmith.ledgerflow.health.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response body for health check")
public class HealthResponse {

  private String status;
  private Instant timestamp;
  private String service;
  private String version;

  public HealthResponse(String status, Instant timestamp, String service, String version) {
    this.status = status;
    this.timestamp = timestamp;
    this.service = service;
    this.version = version;
  }

  public String getStatus() {
    return status;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public String getService() {
    return service;
  }

  public String getVersion() {
    return version;
  }
}
