package dev.pafsmith.ledgerflow.category.dto;

import java.time.Instant;
import java.util.UUID;

import dev.pafsmith.ledgerflow.category.enums.CategoryType;

public class CategoryResponse {

  private UUID id;
  private UUID userId;
  private String name;
  private CategoryType type;
  private boolean systemDefined;
  private Instant createdAt;
  private Instant updatedAt;

  public CategoryResponse() {
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CategoryType getType() {
    return type;
  }

  public void setType(CategoryType type) {
    this.type = type;
  }

  public boolean isSystemDefined() {
    return systemDefined;
  }

  public void setSystemDefined(boolean systemDefined) {
    this.systemDefined = systemDefined;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

}
