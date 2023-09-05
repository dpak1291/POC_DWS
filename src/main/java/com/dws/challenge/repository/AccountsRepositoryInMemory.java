package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.service.NotificationService;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.math.BigDecimal;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

     @Autowired
     public AccountsRepositoryInMemory (NotificationService notificationService ) {
      this.notificationService = notificationService;
     }

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

    @Override
    public void transferBalance(String accountFrom, String accountTo, BigDecimal amount) throws InsufficientBalanceException {
        if(!accountFrom.equals(accountTo)) {
	  
	Account fromAccount = getAccount(accountFrom);
        Account toAccount= getAccount(accountTo);
        
	BigDecimal fromAccountTxnBalance =  fromAccount.getBalance().subtract(amount);
        if(fromAccountTxnBalance > 0) {
          fromAccount.setBalance(fromAccountTxnBalance);
	  accounts.put(fromAccount.getAccountId(), fromAccount);
	  notificationService.notifyAboutTransfer(fromAccount, "Amount debited and transferred to recepient "+ toAccount.getAccountId()+ " : "+amount);
          BigDecimal ToAccountTxnBalance = toAccount.getBalance().add(amount);
	  toAccount.setBalance(ToAccountTxnBalance);
          accounts.put(toAccount.getAccountId(), toAccount);
	  notificationService.notifyAboutTransfer(fromAccount, "Amount credited and transferred from recepient " +fromAccount.getAccountId()+ ":" +amount);
        }
	else {
          throw new InsufficientBalanceException(
		 "From Account id " + accountFrom + " has insufficent balance" +fromAccount.getBalance());	
	}
      }
      else {
	throw new DuplicateAccountIdException(" From and To account ID are duplicated");
    }

}
