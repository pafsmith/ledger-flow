package dev.pafsmith.ledgerflow.account.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import dev.pafsmith.ledgerflow.account.enums.AccountType;
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
@Table(name = "accounts")
public class Account extends BaseEntity {

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private AccountType type;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(name = "opening_balance", nullable = false, precision = 19, scale = 2)
  private BigDecimal openingBalance = BigDecimal.ZERO;

  @Column(nullable = false)
  private boolean active = false;

  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<Transaction> transactions = new ArrayList<>();

  public Account() {

  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public BigDecimal getOpeningBalance() {
    return openingBalance;
  }

  public void setOpeningBalance(BigDecimal openingBalance) {
    this.openingBalance = openingBalance;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public AccountType getType() {
    return type;
  }

  public void setType(AccountType type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

}
