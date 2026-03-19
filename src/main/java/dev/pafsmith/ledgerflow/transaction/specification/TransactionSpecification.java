package dev.pafsmith.ledgerflow.transaction.specification;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import dev.pafsmith.ledgerflow.transaction.dto.TransactionFilterRequest;
import dev.pafsmith.ledgerflow.transaction.entity.Transaction;

public class TransactionSpecification {

  public static Specification<Transaction> withFilters(UUID userId, TransactionFilterRequest filter) {

    return Specification
        .where(hasUserId(userId))
        .and(hasAccountId(filter.getAccountId()))
        .and(hasCategoryId(filter.getCategoryId()))
        .and(hasType(filter.getType()))
        .and(hasDateFrom(filter.getFrom()))
        .and(hasDateTo(filter.getTo()))
        .and(hasMinAmount(filter.getMinAmount()))
        .and(hasMaxAmount(filter.getMaxAmount()));
  }

  private static Specification<Transaction> hasUserId(UUID userId) {
    return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
  }

  private static Specification<Transaction> hasAccountId(UUID accountId) {
    return (root, query, cb) -> accountId == null ? null : cb.equal(root.get("account").get("id"), accountId);
  }

  private static Specification<Transaction> hasCategoryId(UUID categoryId) {
    return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
  }

  private static Specification<Transaction> hasType(dev.pafsmith.ledgerflow.transaction.enums.TransactionType type) {
    return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
  }

  private static Specification<Transaction> hasDateFrom(java.time.LocalDate from) {
    return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("transactionDate"), from);
  }

  private static Specification<Transaction> hasDateTo(java.time.LocalDate to) {
    return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("transactionDate"), to);
  }

  private static Specification<Transaction> hasMinAmount(java.math.BigDecimal minAmount) {
    return (root, query, cb) -> minAmount == null ? null : cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
  }

  private static Specification<Transaction> hasMaxAmount(java.math.BigDecimal maxAmount) {
    return (root, query, cb) -> maxAmount == null ? null : cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
  }
}
