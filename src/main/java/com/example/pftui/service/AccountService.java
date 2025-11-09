package com.example.pftui.service;

import com.example.pftui.model.Account;
import com.example.pftui.model.AccountStatus;
import com.example.pftui.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import com.example.pftui.model.Category; // Required for transaction type check
import com.example.pftui.model.CategoryType;
import com.example.pftui.model.Transaction;
import com.example.pftui.repository.CategoryRepository;
import com.example.pftui.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    
    public AccountService(AccountRepository accountRepository, 
                        TransactionRepository transactionRepository,
                        CategoryRepository categoryRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }
    
    public List<Account> getAllAccountsByUser(String userId) {
        return accountRepository.findByUserId(userId);
    }
    
    public List<Account> getActiveAccountsByUser(String userId) {
        return accountRepository.findByUserIdAndStatus(userId, AccountStatus.ACTIVE);
    }
    
    public Optional<Account> getAccountById(String id) {
        return accountRepository.findById(id);
    }
    
    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }
    
    public Account updateAccount(Account account) {
        return accountRepository.save(account);
    }
    
    public void deleteAccount(String id) {
        accountRepository.deleteById(id);
    }
    
    public long countAccountsByUser(String userId) {
        return accountRepository.countByUserId(userId);
    }

    public List<Account> getAccountsWithBalance(String userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        // Efficiently get all needed categories
        Set<String> categoryIds = transactions.stream()
                .map(Transaction::getCategoryId)
                .collect(Collectors.toSet());
        Map<String, Category> categoryMap = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        // Calculate balance for each account
        for (Account account : accounts) {
            BigDecimal balance = account.getInitialBalance();

            for (Transaction t : transactions) {
                if (t.getAccountId().equals(account.getId())) {
                    Category category = categoryMap.get(t.getCategoryId());
                    if (category != null) {
                        if (category.getType() == CategoryType.INCOME) {
                            balance = balance.add(t.getAmount());
                        } else if (category.getType() == CategoryType.EXPENSE) {
                            balance = balance.subtract(t.getAmount());
                        }
                    }
                }
            }
            account.setCurrentBalance(balance);
        }

        return accounts;
    }
}

