package dev.pafsmith.ledgerflow.transaction.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.pafsmith.ledgerflow.account.entity.Account;
import dev.pafsmith.ledgerflow.account.repository.AccountRepository;
import dev.pafsmith.ledgerflow.category.entity.Category;
import dev.pafsmith.ledgerflow.category.enums.CategoryType;
import dev.pafsmith.ledgerflow.category.repository.CategoryRepository;
import dev.pafsmith.ledgerflow.common.exception.BadRequestException;
import dev.pafsmith.ledgerflow.common.exception.ForbiddenException;
import dev.pafsmith.ledgerflow.common.exception.ResourceNotFoundException;
import dev.pafsmith.ledgerflow.transaction.dto.CreateTransactionRequest;
import dev.pafsmith.ledgerflow.transaction.dto.TransactionResponse;
import dev.pafsmith.ledgerflow.transaction.dto.UpdateTransactionRequest;
import dev.pafsmith.ledgerflow.transaction.entity.Transaction;
import dev.pafsmith.ledgerflow.transaction.enums.TransactionType;
import dev.pafsmith.ledgerflow.transaction.repository.TransactionRepository;
import dev.pafsmith.ledgerflow.user.entity.User;
import dev.pafsmith.ledgerflow.user.repository.UserRepository;

@Service
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final UserRepository userRepository;
  private final AccountRepository accountRepository;
  private final CategoryRepository categoryRepository;

  public TransactionService(
      TransactionRepository transactionRepository,
      UserRepository userRepository,
      AccountRepository accountRepository,
      CategoryRepository categoryRepository) {

    this.transactionRepository = transactionRepository;
    this.userRepository = userRepository;
    this.accountRepository = accountRepository;
    this.categoryRepository = categoryRepository;
  }

  public TransactionResponse createTransaction(CreateTransactionRequest request, String userEmail) {

    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Account account = accountRepository.findById(request.getAccountId())
        .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

    if (!account.getUser().getId().equals(user.getId())) {
      throw new ForbiddenException("Account does not belong to user");
    }

    Category category = null;
    if (request.getCategoryId() != null) {
      category = categoryRepository.findById(request.getCategoryId())
          .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

      if (!category.getUser().getId().equals(user.getId())) {
        throw new ForbiddenException("Category does not belong to user");
      }
    }

    Account destinationAccount = null;
    if (request.getDestinationAccountId() != null) {
      destinationAccount = accountRepository.findById(request.getDestinationAccountId())
          .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));

      if (!destinationAccount.getUser().getId().equals(user.getId())) {
        throw new ForbiddenException("Destination account does not belong to user");
      }
    }

    validateTransactionRules(
        request.getAmount(),
        request.getDescription(),
        request.getTransactionDate(),
        request.getType(),
        category,
        destinationAccount,
        account);

    Transaction transaction = new Transaction();
    transaction.setUser(user);
    transaction.setAccount(account);
    transaction.setCategory(category);
    transaction.setDestinationAccount(destinationAccount);
    transaction.setDescription(request.getDescription());
    transaction.setAmount(request.getAmount());
    transaction.setType(request.getType());
    transaction.setTransactionDate(request.getTransactionDate());
    transaction.setMerchant(request.getMerchant());
    transaction.setNotes(request.getNotes());

    Transaction savedTransaction = transactionRepository.save(transaction);

    return mapToResponse(savedTransaction);
  }

  private void validateTransactionRules(
      java.math.BigDecimal amount,
      String description,
      java.time.LocalDate transactionDate,
      TransactionType type,
      Category category,
      Account destinationAccount,
      Account sourceAccount) {
    if (amount == null || amount.signum() <= 0) {
      throw new BadRequestException("Amount must be greater than zero");
    }

    if (description == null || description.isBlank()) {
      throw new BadRequestException("Description is required");
    }

    if (transactionDate == null) {
      throw new BadRequestException("Transaction date is required");
    }

    if (type == null) {
      throw new BadRequestException("Transaction type is required");
    }

    if (type == TransactionType.TRANSFER) {
      if (destinationAccount == null) {
        throw new BadRequestException("Destination account is required for transfers");
      }

      if (sourceAccount.getId().equals(destinationAccount.getId())) {
        throw new BadRequestException("Source and destination accounts cannot be the same");
      }

      if (category != null) {
        throw new BadRequestException("Transfers should not have a category");
      }
    }

    if (type == TransactionType.EXPENSE) {
      if (destinationAccount != null) {
        throw new BadRequestException("Expenses cannot have a destination account");
      }

      if (category == null) {
        throw new BadRequestException("Expense transactions require a category");
      }

      if (category.getType() != CategoryType.EXPENSE) {
        throw new BadRequestException("Expense transactions must use an expense category");
      }
    }

    if (type == TransactionType.INCOME) {
      if (destinationAccount != null) {
        throw new BadRequestException("Income transactions cannot have a destination account");
      }

      if (category == null) {
        throw new BadRequestException("Income transactions require a category");
      }

      if (category.getType() != CategoryType.INCOME) {
        throw new BadRequestException("Income transactions must use an income category");
      }
    }
  }

  private TransactionResponse mapToResponse(Transaction transaction) {
    TransactionResponse response = new TransactionResponse();
    response.setId(transaction.getId());
    response.setUserId(transaction.getUser().getId());
    response.setAccountId(transaction.getAccount().getId());
    response.setCategoryId(
        transaction.getCategory() != null ? transaction.getCategory().getId() : null);
    response.setDestinationAccountId(
        transaction.getDestinationAccount() != null ? transaction.getDestinationAccount().getId() : null);
    response.setDescription(transaction.getDescription());
    response.setAmount(transaction.getAmount());
    response.setType(transaction.getType());
    response.setTransactionDate(transaction.getTransactionDate());
    response.setMerchant(transaction.getMerchant());
    response.setNotes(transaction.getNotes());
    response.setCreatedAt(transaction.getCreatedAt());
    response.setUpdatedAt(transaction.getUpdatedAt());
    return response;
  }

  public TransactionResponse getTransactionById(UUID transactionId) {
    Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    return mapToResponse(transaction);
  }

  public List<TransactionResponse> getTransactionsForUser(UUID userId) {
    return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId)
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  public List<TransactionResponse> getTransactionsForAccount(UUID accountId) {
    return transactionRepository.findByAccountIdOrderByTransactionDateDesc(accountId)
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  public List<TransactionResponse> getTransactionsForCategory(UUID categoryId) {
    return transactionRepository.findByCategoryIdOrderByTransactionDateDesc(categoryId)
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  public List<TransactionResponse> getTransactionsForUserByType(UUID userId, TransactionType type) {
    return transactionRepository.findByUserIdAndTypeOrderByTransactionDateDesc(userId, type)
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  public List<TransactionResponse> getTransactionsForUserBetweenDates(
      UUID userId,
      LocalDate startDate,
      LocalDate endDate) {
    return transactionRepository
        .findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, startDate, endDate)
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  public TransactionResponse updateTransaction(UUID transactionId, UpdateTransactionRequest request) {

    Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Account account = accountRepository.findById(request.getAccountId())
        .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

    if (!account.getUser().getId().equals(user.getId())) {
      throw new ForbiddenException("Account does not belong to user");
    }
    Category category = null;
    if (request.getCategoryId() != null) {
      category = categoryRepository.findById(request.getCategoryId())
          .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

      if (!category.getUser().getId().equals(user.getId())) {
        throw new ForbiddenException("Category does not belong to user");
      }
    }

    Account destinationAccount = null;
    if (request.getDestinationAccountId() != null) {
      destinationAccount = accountRepository.findById(request.getDestinationAccountId())
          .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));

      if (!destinationAccount.getUser().getId().equals(user.getId())) {
        throw new ForbiddenException("Destination account does not belong to user");
      }
    }
    validateTransactionRules(
        request.getAmount(),
        request.getDescription(),
        request.getTransactionDate(),
        request.getType(),
        category,
        destinationAccount,
        account);

    transaction.setUser(user);
    transaction.setAccount(account);
    transaction.setCategory(category);
    transaction.setDestinationAccount(destinationAccount);
    transaction.setDescription(request.getDescription().trim());
    transaction.setAmount(request.getAmount());
    transaction.setType(request.getType());
    transaction.setTransactionDate(request.getTransactionDate());
    transaction.setMerchant(request.getMerchant());
    transaction.setNotes(request.getNotes());

    Transaction updatedTransaction = transactionRepository.save(transaction);

    return mapToResponse(updatedTransaction);
  }

  public void deleteTransaction(UUID transactionId) {
    Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

    transactionRepository.delete(transaction);
  }
}
