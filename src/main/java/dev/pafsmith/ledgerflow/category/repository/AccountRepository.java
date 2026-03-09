package dev.pafsmith.ledgerflow.category.repository;

import dev.pafsmith.ledgerflow.category.entity.Category;
import dev.pafsmith.ledgerflow.category.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Category, UUID> {
    List<Category> findByUserId(UUID userID);

    List<Category> findByUserIdAndType(UUID userId, CategoryType type);
}
