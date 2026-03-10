package dev.pafsmith.ledgerflow.category.entity;

import java.util.ArrayList;
import java.util.List;

import dev.pafsmith.ledgerflow.budgets.entity.Budget;
import dev.pafsmith.ledgerflow.category.enums.CategoryType;
import dev.pafsmith.ledgerflow.common.model.BaseEntity;
import dev.pafsmith.ledgerflow.transaction.entity.Transaction;
import dev.pafsmith.ledgerflow.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private CategoryType type;

  @Column(name = "system_defined", nullable = false)
  private boolean systemDefined = false;

  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<Transaction> transactions = new ArrayList<>();

  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<Budget> budgets = new ArrayList<>();

  public Category() {

  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
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

}
