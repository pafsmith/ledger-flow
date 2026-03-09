package dev.pafsmith.ledgerflow.account.service;

import dev.pafsmith.ledgerflow.account.entity.Account;
import dev.pafsmith.ledgerflow.account.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAccountsForUser(UUID userId) {
       return accountRepository.findByUserId(userId);
    }
}
