package com.example.pftui.config;

import com.example.pftui.model.*;
import com.example.pftui.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Starting Data Initialization ===");
        
        // Create default admin user if not exists
        User admin = null;
        if (!userRepository.existsByEmail("admin@pft.com")) {
            admin = new User();
            admin.setEmail("admin@pft.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Admin User");
            admin.setRole("ADMIN");
            admin.setCreatedAt(LocalDateTime.now());
            admin.setEnabled(true); // Explicitly enable admin user
            admin = userRepository.save(admin);
            System.out.println("✓ Default admin user created: admin@pft.com / admin123");
        } else {
            admin = userRepository.findByEmail("admin@pft.com").orElse(null);
        }

        // Create default test user if not exists
        User user = null;
        if (!userRepository.existsByEmail("user@pft.com")) {
            user = new User();
            user.setEmail("user@pft.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setFullName("Test User");
            user.setRole("USER");
            user.setCreatedAt(LocalDateTime.now());
            user.setEnabled(true); // Explicitly enable test user
            user = userRepository.save(user);
            System.out.println("✓ Default test user created: user@pft.com / user123");
        } else {
            user = userRepository.findByEmail("user@pft.com").orElse(null);
        }
        
        // Initialize system categories if not exists
        if (categoryRepository.findByIsSystemTrue().isEmpty()) {
            initializeSystemCategories();
            System.out.println("✓ System categories initialized");
        }
        
        // Initialize sample data for test user
        if (user != null && accountRepository.countByUserId(user.getId()) == 0) {
            initializeSampleData(user);
            System.out.println("✓ Sample data initialized for test user");
        }

        System.out.println("=== Data Initialization Completed! ===");
    }
    
    private void initializeSystemCategories() {
        List<Category> categories = new ArrayList<>();
        
        // Income categories
        categories.add(createCategory(null, "Salary", CategoryType.INCOME, "ri-money-dollar-circle-line", true));
        categories.add(createCategory(null, "Freelance", CategoryType.INCOME, "ri-briefcase-line", true));
        categories.add(createCategory(null, "Investment", CategoryType.INCOME, "ri-line-chart-line", true));
        categories.add(createCategory(null, "Gift", CategoryType.INCOME, "ri-gift-line", true));
        categories.add(createCategory(null, "Other Income", CategoryType.INCOME, "ri-add-circle-line", true));
        
        // Expense categories
        categories.add(createCategory(null, "Food & Dining", CategoryType.EXPENSE, "ri-restaurant-line", true));
        categories.add(createCategory(null, "Shopping", CategoryType.EXPENSE, "ri-shopping-cart-line", true));
        categories.add(createCategory(null, "Transportation", CategoryType.EXPENSE, "ri-car-line", true));
        categories.add(createCategory(null, "Entertainment", CategoryType.EXPENSE, "ri-movie-line", true));
        categories.add(createCategory(null, "Bills & Utilities", CategoryType.EXPENSE, "ri-file-list-line", true));
        categories.add(createCategory(null, "Healthcare", CategoryType.EXPENSE, "ri-heart-pulse-line", true));
        categories.add(createCategory(null, "Education", CategoryType.EXPENSE, "ri-book-line", true));
        categories.add(createCategory(null, "Travel", CategoryType.EXPENSE, "ri-plane-line", true));
        categories.add(createCategory(null, "Other Expense", CategoryType.EXPENSE, "ri-subtract-line", true));
        
        categoryRepository.saveAll(categories);
    }
    
    private Category createCategory(String userId, String name, CategoryType type, String icon, boolean isSystem) {
        Category cat = new Category();
        cat.setUserId(userId);
        cat.setName(name);
        cat.setType(type);
        cat.setIcon(icon);
        cat.setSystem(isSystem);
        return cat;
    }
    
    private void initializeSampleData(User user) {
        String userId = user.getId();
        
        // Create sample accounts
        Account checking = new Account();
        checking.setUserId(userId);
        checking.setName("Checking Account");
        checking.setType(AccountType.CHECKING);
        checking.setCurrency("USD");
        checking.setInitialBalance(new BigDecimal("5000.00"));
        checking.setStatus(AccountStatus.ACTIVE);
        checking = accountRepository.save(checking);
        
        Account savings = new Account();
        savings.setUserId(userId);
        savings.setName("Savings Account");
        savings.setType(AccountType.SAVINGS);
        savings.setCurrency("USD");
        savings.setInitialBalance(new BigDecimal("10000.00"));
        savings.setStatus(AccountStatus.ACTIVE);
        savings = accountRepository.save(savings);
        
        Account creditCard = new Account();
        creditCard.setUserId(userId);
        creditCard.setName("Credit Card");
        creditCard.setType(AccountType.CREDIT_CARD);
        creditCard.setCurrency("USD");
        creditCard.setInitialBalance(BigDecimal.ZERO);
        creditCard.setStatus(AccountStatus.ACTIVE);
        creditCard = accountRepository.save(creditCard);
        
        // Get system categories
        List<Category> allCategories = categoryRepository.findByIsSystemTrue();
        Category salary = allCategories.stream().filter(c -> c.getName().equals("Salary")).findFirst().orElse(null);
        Category food = allCategories.stream().filter(c -> c.getName().equals("Food & Dining")).findFirst().orElse(null);
        Category shopping = allCategories.stream().filter(c -> c.getName().equals("Shopping")).findFirst().orElse(null);
        Category transport = allCategories.stream().filter(c -> c.getName().equals("Transportation")).findFirst().orElse(null);
        Category bills = allCategories.stream().filter(c -> c.getName().equals("Bills & Utilities")).findFirst().orElse(null);
        Category entertainment = allCategories.stream().filter(c -> c.getName().equals("Entertainment")).findFirst().orElse(null);
        
        // Create sample transactions for last 6 months
        List<Transaction> transactions = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // Current month transactions
        if (salary != null) {
            transactions.add(createTransaction(userId, checking.getId(), salary.getId(), 
                new BigDecimal("5000.00"), today.withDayOfMonth(1), "Monthly Salary", "Salary payment"));
        }
        
        if (food != null) {
            transactions.add(createTransaction(userId, checking.getId(), food.getId(), 
                new BigDecimal("45.50"), today.minusDays(2), "Restaurant ABC", "Dinner"));
            transactions.add(createTransaction(userId, creditCard.getId(), food.getId(), 
                new BigDecimal("120.00"), today.minusDays(5), "Grocery Store", "Weekly groceries"));
            transactions.add(createTransaction(userId, checking.getId(), food.getId(), 
                new BigDecimal("15.00"), today.minusDays(7), "Coffee Shop", "Morning coffee"));
        }
        
        if (shopping != null) {
            transactions.add(createTransaction(userId, creditCard.getId(), shopping.getId(), 
                new BigDecimal("89.99"), today.minusDays(3), "Online Store", "New shoes"));
            transactions.add(createTransaction(userId, checking.getId(), shopping.getId(), 
                new BigDecimal("250.00"), today.minusDays(10), "Electronics Store", "Headphones"));
        }
        
        if (transport != null) {
            transactions.add(createTransaction(userId, checking.getId(), transport.getId(), 
                new BigDecimal("50.00"), today.minusDays(1), "Gas Station", "Fuel"));
            transactions.add(createTransaction(userId, checking.getId(), transport.getId(), 
                new BigDecimal("25.00"), today.minusDays(8), "Uber", "Ride to office"));
        }
        
        if (bills != null) {
            transactions.add(createTransaction(userId, checking.getId(), bills.getId(), 
                new BigDecimal("150.00"), today.minusDays(15), "Electric Company", "Monthly bill"));
            transactions.add(createTransaction(userId, checking.getId(), bills.getId(), 
                new BigDecimal("60.00"), today.minusDays(12), "Internet Provider", "Monthly subscription"));
        }
        
        if (entertainment != null) {
            transactions.add(createTransaction(userId, creditCard.getId(), entertainment.getId(), 
                new BigDecimal("45.00"), today.minusDays(6), "Cinema", "Movie tickets"));
        }
        
        // Previous months transactions (simplified)
        for (int monthsAgo = 1; monthsAgo <= 5; monthsAgo++) {
            LocalDate monthDate = today.minusMonths(monthsAgo);
            
            if (salary != null) {
                transactions.add(createTransaction(userId, checking.getId(), salary.getId(), 
                    new BigDecimal("5000.00"), monthDate.withDayOfMonth(1), "Monthly Salary", "Salary payment"));
            }
            
            if (food != null) {
                transactions.add(createTransaction(userId, checking.getId(), food.getId(), 
                    new BigDecimal("300.00"), monthDate.withDayOfMonth(10), "Various", "Food expenses"));
            }
            
            if (shopping != null) {
                transactions.add(createTransaction(userId, creditCard.getId(), shopping.getId(), 
                    new BigDecimal("200.00"), monthDate.withDayOfMonth(15), "Various", "Shopping"));
            }
            
            if (transport != null) {
                transactions.add(createTransaction(userId, checking.getId(), transport.getId(), 
                    new BigDecimal("150.00"), monthDate.withDayOfMonth(20), "Various", "Transport"));
            }
            
            if (bills != null) {
                transactions.add(createTransaction(userId, checking.getId(), bills.getId(), 
                    new BigDecimal("210.00"), monthDate.withDayOfMonth(5), "Various", "Bills"));
            }
        }
        
        transactionRepository.saveAll(transactions);
        
        // Create sample budgets for current month
        List<Budget> budgets = new ArrayList<>();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();
        
        if (food != null) {
            budgets.add(createBudget(userId, food.getId(), currentMonth, currentYear, new BigDecimal("500.00")));
        }
        if (shopping != null) {
            budgets.add(createBudget(userId, shopping.getId(), currentMonth, currentYear, new BigDecimal("300.00")));
        }
        if (transport != null) {
            budgets.add(createBudget(userId, transport.getId(), currentMonth, currentYear, new BigDecimal("200.00")));
        }
        if (bills != null) {
            budgets.add(createBudget(userId, bills.getId(), currentMonth, currentYear, new BigDecimal("250.00")));
        }
        if (entertainment != null) {
            budgets.add(createBudget(userId, entertainment.getId(), currentMonth, currentYear, new BigDecimal("150.00")));
        }
        
        budgetRepository.saveAll(budgets);
    }
    
    private Transaction createTransaction(String userId, String accountId, String categoryId, 
                                         BigDecimal amount, LocalDate date, String merchant, String note) {
        Transaction t = new Transaction();
        t.setUserId(userId);
        t.setAccountId(accountId);
        t.setCategoryId(categoryId);
        t.setAmount(amount);
        t.setDate(date);
        t.setMerchant(merchant);
        t.setNote(note);
        t.setRecurring(false);
        return t;
    }
    
    private Budget createBudget(String userId, String categoryId, int month, int year, BigDecimal limit) {
        Budget b = new Budget();
        b.setUserId(userId);
        b.setCategoryId(categoryId);
        b.setMonth(month);
        b.setYear(year);
        b.setLimitAmount(limit);
        return b;
    }
}

