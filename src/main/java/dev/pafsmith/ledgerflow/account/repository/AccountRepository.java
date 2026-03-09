package dev.pafsmith.ledgerflow.account.repository;

import dev.pafsmith.ledgerflow.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByUserId(UUID userId);

}
