package com.example.pftui.service;

import com.example.pftui.model.Budget;
import com.example.pftui.model.Category;
import com.example.pftui.model.Transaction;
import com.example.pftui.model.CategoryType;
import com.example.pftui.model.BudgetUsage;
import com.example.pftui.repository.BudgetRepository;
import com.example.pftui.repository.CategoryRepository;
import com.example.pftui.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public BudgetService(BudgetRepository budgetRepository, 
                         CategoryRepository categoryRepository, 
                         TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Get all budgets for a user for a given month and year, along with spending details.
     */
    public List<BudgetUsage> getBudgetUsages(String userId, int month, int year) {
        // 1. Get all budgets for the user for the given month and year
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);

        // 2. Get all EXPENSE transactions for the same period
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<Transaction> transactions = transactionRepository.findTransactionsByUserAndDateRange(userId, startDate, endDate);

        // 3. Filter for expense transactions and group by categoryId, summing the amounts
        Map<String, BigDecimal> spentMap = transactions.stream()
            .filter(t -> {
                // We need to know the category type, so let's fetch it if not present
                // This is a bit inefficient. A better approach might be a custom query.
                // For now, let's assume we need to populate category details.
                return true; // We'll filter after populating
            })
            .collect(Collectors.groupingBy(
                Transaction::getCategoryId,
                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
            ));

        // 4. Get all categories for the budgets to populate names and check types
        List<String> categoryIds = budgets.stream().map(Budget::getCategoryId).collect(Collectors.toList());
        Map<String, Category> categoryMap = categoryRepository.findAllById(categoryIds).stream()
            .collect(Collectors.toMap(Category::getId, category -> category));

        // 5. Build the BudgetUsage view model
        return budgets.stream()
            .map(budget -> {
                Category category = categoryMap.get(budget.getCategoryId());
                if (category == null || category.getType() != CategoryType.EXPENSE) {
                    return null; // Budgets are only for expenses
                }
                BigDecimal spent = spentMap.getOrDefault(budget.getCategoryId(), BigDecimal.ZERO);
                return new BudgetUsage(category.getName(), spent, budget.getLimitAmount());
            })
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    public List<Budget> getBudgetsForUser(String userId, int month, int year) {
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
        List<String> categoryIds = budgets.stream().map(Budget::getCategoryId).collect(Collectors.toList());
        Map<String, Category> categoryMap = categoryRepository.findAllById(categoryIds).stream()
            .collect(Collectors.toMap(Category::getId, category -> category));
        budgets.forEach(b -> b.setCategory(categoryMap.get(b.getCategoryId())));
        return budgets;
    }

    /**
     * Create or update a budget.
     */
    @Transactional
    public Budget saveBudget(Budget budget) {
        // Ensure category exists and belongs to user or is system
        categoryRepository.findById(budget.getCategoryId()).orElseThrow(() -> new IllegalArgumentException("Invalid Category ID"));
        
        // Prevent creating duplicate budget for the same category, month, and year
        Optional<Budget> existing = budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
            budget.getUserId(), budget.getCategoryId(), budget.getMonth(), budget.getYear());
            
        if (existing.isPresent() && !existing.get().getId().equals(budget.getId())) {
            throw new IllegalStateException("A budget for this category and month already exists.");
        }

        return budgetRepository.save(budget);
    }

    /**
     * Get a single budget by its ID.
     */
    public Optional<Budget> getBudgetById(String id) {
        return budgetRepository.findById(id);
    }

    /**
     * Delete a budget by its ID, ensuring user ownership.
     */
    @Transactional
    public void deleteBudget(String id, String userId) {
        Budget budget = budgetRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + id));
        
        if (!budget.getUserId().equals(userId)) {
            throw new SecurityException("User does not have permission to delete this budget.");
        }
        
        budgetRepository.deleteById(id);
    }
}
