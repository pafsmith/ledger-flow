package dev.pafsmith.ledgerflow.budgets.entity;

import java.math.BigDecimal;

import dev.pafsmith.ledgerflow.category.entity.Category;
import dev.pafsmith.ledgerflow.common.model.BaseEntity;
import dev.pafsmith.ledgerflow.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "budgets", uniqueConstraints = {
    @UniqueConstraint(name = "uq_budgets_user_category_year_month", columnNames = { "user_id", "category_id", "year",
        "month" })
})
public class Budget extends BaseEntity {
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @Column(name = "limit_amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal limitAmount;

  @Column(nullable = false)
  private Integer year;

  @Column(nullable = false)
  private Integer month;

  public Budget() {
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public BigDecimal getLimitAmount() {
    return limitAmount;
  }

  public void setLimitAmount(BigDecimal limitAmount) {
    this.limitAmount = limitAmount;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public Integer getMonth() {
    return month;
  }

  public void setMonth(Integer month) {
    this.month = month;
  }

}
