package com.example.pftui.service;

import com.example.pftui.model.*;
import com.example.pftui.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    
    public DashboardService(TransactionRepository transactionRepository,
                          AccountRepository accountRepository,
                          CategoryRepository categoryRepository,
                          BudgetRepository budgetRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
    }
    
    public DashboardVM buildDashboard(String userId) {
        DashboardVM vm = new DashboardVM();
        LocalDate now = LocalDate.now();

        // --- 1. Fetch all data in a single, larger query (last 6 months) ---
        LocalDate sixMonthsAgo = now.minusMonths(5).withDayOfMonth(1);
        List<Transaction> allTransactions = transactionRepository.findByUserIdAndDateBetween(userId, sixMonthsAgo, now.withDayOfMonth(now.lengthOfMonth()));
        populateTransactionDetails(allTransactions);

        // --- 2. Process data for the current month ---
        YearMonth currentYm = YearMonth.from(now);
        List<Transaction> monthTransactions = allTransactions.stream()
            .filter(t -> YearMonth.from(t.getDate()).equals(currentYm))
            .collect(Collectors.toList());

        // Calculate MTD income and expense
        vm.mtdIncome = monthTransactions.stream()
            .filter(t -> t.getCategory() != null && t.getCategory().getType() == CategoryType.INCOME)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        vm.mtdExpense = monthTransactions.stream()
            .filter(t -> t.getCategory() != null && t.getCategory().getType() == CategoryType.EXPENSE)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        vm.net = vm.mtdIncome.subtract(vm.mtdExpense);

        // Category breakdown (expenses only for pie chart)
        Map<String, BigDecimal> categoryMap = monthTransactions.stream()
            .filter(t -> t.getCategory() != null && t.getCategory().getType() == CategoryType.EXPENSE)
            .collect(Collectors.groupingBy(
                t -> t.getCategory().getName(),
                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
            ));
        
        vm.categoryLabels = new ArrayList<>(categoryMap.keySet());
        vm.categoryValues = new ArrayList<>(categoryMap.values());

        // --- 3. Process data for the cash flow trend (last 6 months) ---
        Map<YearMonth, Map<CategoryType, BigDecimal>> monthlySummary = allTransactions.stream()
            .filter(t -> t.getCategory() != null)
            .collect(Collectors.groupingBy(
                t -> YearMonth.from(t.getDate()),
                Collectors.groupingBy(
                    t -> t.getCategory().getType(),
                    Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                )
            ));

        vm.months = new ArrayList<>();
        vm.incomes = new ArrayList<>();
        vm.expenses = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            Map<CategoryType, BigDecimal> summary = monthlySummary.getOrDefault(ym, Collections.emptyMap());
            
            vm.months.add(ym.getMonth().toString().substring(0, 3));
            vm.incomes.add(summary.getOrDefault(CategoryType.INCOME, BigDecimal.ZERO));
            vm.expenses.add(summary.getOrDefault(CategoryType.EXPENSE, BigDecimal.ZERO));
        }

        // --- 4. Get recent transactions (can be a separate query as it's small) ---
        List<Transaction> recentTxns = transactionRepository.findByUserIdOrderByDateDesc(userId, PageRequest.of(0, 10));
        populateTransactionDetails(recentTxns);
        vm.recentTransactions = recentTxns;

        // --- 5. Build budget usage for the current month ---
        vm.budgetUsages = buildBudgetUsages(userId, now.getMonthValue(), now.getYear(), monthTransactions);

        return vm;
    }
    
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
    
    private List<BudgetUsage> buildBudgetUsages(String userId, int month, int year, List<Transaction> monthTransactions) {
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
        
        if (budgets.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Populate category info for budgets
        Set<String> categoryIds = budgets.stream()
            .map(Budget::getCategoryId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        Map<String, Category> categoryMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            categoryRepository.findAllById(categoryIds)
                .forEach(cat -> categoryMap.put(cat.getId(), cat));
        }
        
        for (Budget b : budgets) {
            if (b.getCategoryId() != null) {
                b.setCategory(categoryMap.get(b.getCategoryId()));
            }
        }
        
        // Calculate spending per category
        Map<String, BigDecimal> spentMap = new HashMap<>();
        for (Transaction t : monthTransactions) {
            if (t.getCategoryId() != null && t.getCategory() != null 
                && t.getCategory().getType() == CategoryType.EXPENSE) {
                spentMap.put(t.getCategoryId(), 
                    spentMap.getOrDefault(t.getCategoryId(), BigDecimal.ZERO).add(t.getAmount()));
            }
        }
        
        List<BudgetUsage> usages = new ArrayList<>();
        for (Budget b : budgets) {
            if (b.getCategory() != null) {
                String categoryName = b.getCategory().getName();
                BigDecimal limit = b.getLimitAmount();
                BigDecimal spent = spentMap.getOrDefault(b.getCategoryId(), BigDecimal.ZERO);
                
                BudgetUsage usage = new BudgetUsage(categoryName, spent, limit);
                usages.add(usage);
            }
        }
        
        return usages;
    }
}

