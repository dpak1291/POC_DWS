package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsuffcientBalanceException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
    notificationService = webApplicationContext.getBean(NotificationService.class);
  } 

  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  void TransferBalance_failsOnDuplicate() {
    Account account1 = new Account("Id-123");
    account1.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account1);

    Account account2 = new Account("Id-456");
    account2.setBalance(new BigDecimal(500));
    this.accountsService.createAccount(account2);

    try {
      this.accountsService.transferBalance("Id-123", "Id-123", 1000);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).contains("duplicated");
    }
  }

  @Test
  void TransferBalance_InsufficientFunds() {
    Account account1 = new Account("Id-123");
    account1.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account1);

    Account account2 = new Account("Id-456");
    account2.setBalance(new BigDecimal(500));
    this.accountsService.createAccount(account2);

    try {
      this.accountsService.transferBalance("Id-123", "Id-456", 2000);
      fail("Should have failed when crossing negative funds");
    } catch (InsufficentBalanceException ex) {
      assertThat(ex.getMessage()).contains("has insufficent balance");
    }
  }
  @Test
  void TransferBalance_SufficientFunds() {
    Account account1 = new Account("Id-123");
    account1.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account1);

    Account account2 = new Account("Id-456");
    account2.setBalance(new BigDecimal(500));
    this.accountsService.createAccount(account2);

    try {
      this.accountsService.transferBalance("Id-123", "Id-456", 2000);
      pass("Funds Transferred successfully");
    } catch (DuplicateAccountIdException ex) {
      assertFalse(ex.getMessage()).contains("has insufficent balance");
    }
  }

}
