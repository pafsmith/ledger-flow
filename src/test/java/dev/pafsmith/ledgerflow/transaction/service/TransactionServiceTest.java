package dev.pafsmith.ledgerflow.transaction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.pafsmith.ledgerflow.account.entity.Account;
import dev.pafsmith.ledgerflow.account.enums.AccountType;
import dev.pafsmith.ledgerflow.account.repository.AccountRepository;
import dev.pafsmith.ledgerflow.category.entity.Category;
import dev.pafsmith.ledgerflow.category.enums.CategoryType;
import dev.pafsmith.ledgerflow.category.repository.CategoryRepository;
import dev.pafsmith.ledgerflow.common.exception.BadRequestException;
import dev.pafsmith.ledgerflow.transaction.dto.CreateTransactionRequest;
import dev.pafsmith.ledgerflow.transaction.entity.Transaction;
import dev.pafsmith.ledgerflow.transaction.enums.TransactionType;
import dev.pafsmith.ledgerflow.transaction.repository.TransactionRepository;
import dev.pafsmith.ledgerflow.user.entity.User;
import dev.pafsmith.ledgerflow.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private TransactionService transactionService;

  private UUID userId;
  private UUID accountId;
  private UUID categoryId;

  private User user;
  private Account account;
  private Category category;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    accountId = UUID.randomUUID();
    categoryId = UUID.randomUUID();

    user = new User();
    user.setId(userId);
    user.setFirstName("Paul");
    user.setLastName("Smith");
    user.setEmail("paul@test.com");
    user.setPasswordHash("hashed");

    account = new Account();
    account.setId(accountId);
    account.setUser(user);
    account.setName("Main Account");
    account.setType(AccountType.CURRENT);
    account.setCurrency("GBP");

    category = new Category();
    category.setId(categoryId);
    category.setUser(user);
    category.setName("Groceries");
    category.setType(CategoryType.EXPENSE);
  }

  @Test
  void createTransaction_shouldSaveExpenseTransactionSuccessfully() {
    CreateTransactionRequest request = new CreateTransactionRequest();
    request.setUserId(userId);
    request.setAccountId(accountId);
    request.setCategoryId(categoryId);
    request.setDescription("Tesco shop");
    request.setAmount(new BigDecimal("45.50"));
    request.setType(TransactionType.EXPENSE);
    request.setTransactionDate(LocalDate.of(2026, 3, 10));
    request.setMerchant("Tesco");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
    when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
      Transaction transaction = invocation.getArgument(0);
      transaction.setId(UUID.randomUUID());
      return transaction;
    });

    var response = transactionService.createTransaction(request);

    assertThat(response).isNotNull();
    assertThat(response.getDescription()).isEqualTo("Tesco shop");
    assertThat(response.getAmount()).isEqualByComparingTo("45.50");
    assertThat(response.getType()).isEqualTo(TransactionType.EXPENSE);

    ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
    verify(transactionRepository).save(captor.capture());

    Transaction savedTransaction = captor.getValue();
    assertThat(savedTransaction.getUser()).isEqualTo(user);
    assertThat(savedTransaction.getAccount()).isEqualTo(account);
    assertThat(savedTransaction.getCategory()).isEqualTo(category);
  }

  @Test
  void createTransaction_shouldThrowWhenTransferHasNoDestinationAccount() {
    CreateTransactionRequest request = new CreateTransactionRequest();
    request.setUserId(userId);
    request.setAccountId(accountId);
    request.setDescription("Move money");
    request.setAmount(new BigDecimal("100.00"));
    request.setType(TransactionType.TRANSFER);
    request.setTransactionDate(LocalDate.of(2026, 3, 10));

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

    assertThatThrownBy(() -> transactionService.createTransaction(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Destination account is required for transfers");

    verify(transactionRepository, never()).save(any(Transaction.class));
  }

  @Test
  void createTransaction_shouldThrowWhenExpenseUsesIncomeCategory() {
    category.setType(CategoryType.INCOME);

    CreateTransactionRequest request = new CreateTransactionRequest();
    request.setUserId(userId);
    request.setAccountId(accountId);
    request.setCategoryId(categoryId);
    request.setDescription("Tesco shop");
    request.setAmount(new BigDecimal("45.50"));
    request.setType(TransactionType.EXPENSE);
    request.setTransactionDate(LocalDate.of(2026, 3, 10));

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

    assertThatThrownBy(() -> transactionService.createTransaction(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Expense transactions must use an expense category");

    verify(transactionRepository, never()).save(any(Transaction.class));
  }
}
