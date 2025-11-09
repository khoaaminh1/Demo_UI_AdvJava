package com.example.pftui.service;

import com.example.pftui.model.Account;
import com.example.pftui.model.Category;
import com.example.pftui.model.Transaction;
import com.example.pftui.repository.AccountRepository;
import com.example.pftui.repository.CategoryRepository;
import com.example.pftui.repository.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    
    public TransactionService(TransactionRepository transactionRepository,
                            AccountRepository accountRepository,
                            CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }
    
    /**
     * Get all transactions for a user, sorted by date descending
     */
    public List<Transaction> getAllTransactionsByUser(String userId) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByDateDesc(userId);
        populateTransactionDetails(transactions);
        return transactions;
    }
    
    /**
     * Get transactions with pagination
     */
    public List<Transaction> getTransactionsByUser(String userId, int page, int size) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByDateDesc(
            userId, 
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"))
        );
        populateTransactionDetails(transactions);
        return transactions;
    }
    
    /**
     * Get transactions by date range
     */
    public List<Transaction> getTransactionsByDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        populateTransactionDetails(transactions);
        return transactions;
    }
    
    /**
     * Get transactions by category
     */
    public List<Transaction> getTransactionsByCategory(String userId, String categoryId) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndCategoryId(userId, categoryId);
        populateTransactionDetails(transactions);
        return transactions;
    }
    
    /**
     * Get transactions by account
     */
    public List<Transaction> getTransactionsByAccount(String userId, String accountId) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndAccountId(userId, accountId);
        populateTransactionDetails(transactions);
        return transactions;
    }
    
    /**
     * Get a single transaction by ID
     */
    public Optional<Transaction> getTransactionById(String id) {
        Optional<Transaction> transaction = transactionRepository.findById(id);
        transaction.ifPresent(t -> populateTransactionDetails(Collections.singletonList(t)));
        return transaction;
    }
    
    /**
     * Create a new transaction
     */
    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        // Validate account and category exist
        if (transaction.getAccountId() != null) {
            accountRepository.findById(transaction.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + transaction.getAccountId()));
        }
        
        if (transaction.getCategoryId() != null) {
            categoryRepository.findById(transaction.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + transaction.getCategoryId()));
        }
        
        Transaction saved = transactionRepository.save(transaction);
        populateTransactionDetails(Collections.singletonList(saved));
        return saved;
    }
    
    /**
     * Update an existing transaction
     */
    @Transactional
    public Transaction updateTransaction(String id, Transaction transaction) {
        Transaction existing = transactionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));
        
        // Verify user owns this transaction
        if (!existing.getUserId().equals(transaction.getUserId())) {
            throw new IllegalArgumentException("Unauthorized to update this transaction");
        }
        
        // Validate account and category exist
        if (transaction.getAccountId() != null) {
            accountRepository.findById(transaction.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + transaction.getAccountId()));
        }
        
        if (transaction.getCategoryId() != null) {
            categoryRepository.findById(transaction.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + transaction.getCategoryId()));
        }
        
        // Update fields
        existing.setAccountId(transaction.getAccountId());
        existing.setCategoryId(transaction.getCategoryId());
        existing.setAmount(transaction.getAmount());
        existing.setDate(transaction.getDate());
        existing.setMerchant(transaction.getMerchant());
        existing.setNote(transaction.getNote());
        existing.setRecurring(transaction.isRecurring());
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        
        Transaction saved = transactionRepository.save(existing);
        populateTransactionDetails(Collections.singletonList(saved));
        return saved;
    }
    
    /**
     * Delete a transaction
     */
    @Transactional
    public void deleteTransaction(String id, String userId) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));
        
        // Verify user owns this transaction
        if (!transaction.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to delete this transaction");
        }
        
        transactionRepository.deleteById(id);
    }
    
    /**
     * Count transactions by user
     */
    public long countTransactionsByUser(String userId) {
        return transactionRepository.countByUserId(userId);
    }
    
    /**
     * Populate transient account and category fields for transactions
     */
    private void populateTransactionDetails(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }
        
        // Get all unique account and category IDs
        Set<String> accountIds = transactions.stream()
            .map(Transaction::getAccountId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        Set<String> categoryIds = transactions.stream()
            .map(Transaction::getCategoryId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        // Fetch accounts and categories in batch
        Map<String, Account> accountMap = new HashMap<>();
        if (!accountIds.isEmpty()) {
            accountRepository.findAllById(accountIds)
                .forEach(acc -> accountMap.put(acc.getId(), acc));
        }
        
        Map<String, Category> categoryMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            categoryRepository.findAllById(categoryIds)
                .forEach(cat -> categoryMap.put(cat.getId(), cat));
        }
        
        // Populate transient fields
        for (Transaction t : transactions) {
            if (t.getAccountId() != null) {
                t.setAccount(accountMap.get(t.getAccountId()));
            }
            if (t.getCategoryId() != null) {
                t.setCategory(categoryMap.get(t.getCategoryId()));
            }
        }
    }
}

