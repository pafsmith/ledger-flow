package dev.pafsmith.ledgerflow.budgets.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.pafsmith.ledgerflow.budgets.entity.Budget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

  List<Budget> findByUserId(UUID userId);

  List<Budget> findByUserIdAndYearAndMonth(UUID userId, Integer year, Integer month);

  List<Budget> findByUserIdAndYear(UUID userId, Integer year);

  List<Budget> findByUserIdAndMonth(UUID userId, Integer month);

  Optional<Budget> findByUserIdAndCategoryIdAndYearAndMonth(
      UUID userId,
      UUID categoryId,
      Integer year,
      Integer month);

  boolean existsByUserIdAndCategoryIdAndYearAndMonth(
      UUID userId,
      UUID categoryId,
      Integer year,
      Integer month);

  boolean existsByUserIdAndCategoryIdAndYearAndMonthAndIdNot(
      UUID userId,
      UUID categoryId,
      Integer year,
      Integer month,
      UUID id);
}
