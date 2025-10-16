package com.example.pftui.service;

import com.example.pftui.model.*;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class FakeDataService {

    private final List<Category> categories = new CopyOnWriteArrayList<>();
    private final List<Account> accounts = new CopyOnWriteArrayList<>();
    private final List<Transaction> transactions = new CopyOnWriteArrayList<>();
    private final List<Budget> budgets = new CopyOnWriteArrayList<>();

    public List<Category> getCategories() { return categories; }
    public List<Account> getAccounts() { return accounts; }
    public List<Transaction> getTransactions() { return transactions; }
    public List<Budget> getBudgets() { return budgets; }

    @PostConstruct
    public void seed() {
        // Categories
        Category food = new Category(UUID.randomUUID(), "Food & Drinks", CategoryType.EXPENSE, "ri-restaurant-2-line");
        Category rent = new Category(UUID.randomUUID(), "Rent", CategoryType.EXPENSE, "ri-home-4-line");
        Category transport = new Category(UUID.randomUUID(), "Transport", CategoryType.EXPENSE, "ri-taxi-line");
        Category shopping = new Category(UUID.randomUUID(), "Shopping", CategoryType.EXPENSE, "ri-shopping-bag-3-line");
        Category salary = new Category(UUID.randomUUID(), "Salary", CategoryType.INCOME, "ri-money-dollar-circle-line");
        categories.addAll(Arrays.asList(food, rent, transport, shopping, salary));

        // Accounts
        Account wallet = new Account(UUID.randomUUID(), "Wallet", AccountType.CASH, "USD");
        Account bank = new Account(UUID.randomUUID(), "Bank", AccountType.BANK, "USD");
        accounts.addAll(Arrays.asList(wallet, bank));

        // Budgets (current month)
        YearMonth ym = YearMonth.now();
        budgets.add(new Budget(UUID.randomUUID(), food, ym.getMonthValue(), ym.getYear(), new BigDecimal("250.00")));
        budgets.add(new Budget(UUID.randomUUID(), transport, ym.getMonthValue(), ym.getYear(), new BigDecimal("120.00")));
        budgets.add(new Budget(UUID.randomUUID(), shopping, ym.getMonthValue(), ym.getYear(), new BigDecimal("200.00")));

        // Transactions (random last ~5 months)
        LocalDate now = LocalDate.now();
        Random rnd = new Random(42);
        for (int i=0;i<180;i++) {
            LocalDate d = now.minusDays(rnd.nextInt(150));
            Category c;
            BigDecimal amt;
            String merchant;
            if (rnd.nextDouble() < 0.15) {
                c = salary; amt = new BigDecimal(1000 + rnd.nextInt(1000));
                merchant = "Company Payroll";
            } else {
                int k = rnd.nextInt(3);
                if (k==0) { c = food; merchant = "Restaurant"; }
                else if (k==1) { c = transport; merchant = "Grab/Taxi"; }
                else { c = shopping; merchant = "Store"; }
                amt = new BigDecimal(5 + rnd.nextInt(80));
            }
            Account acc = rnd.nextBoolean() ? wallet : bank;
            transactions.add(new Transaction(UUID.randomUUID(), acc, c, amt, d, merchant, "", false));
        }
        // Rent monthly
        for (int m=0;m<5;m++) {
            LocalDate dd = YearMonth.now().minusMonths(m).atDay(1);
            transactions.add(new Transaction(UUID.randomUUID(), bank, rent, new BigDecimal("400.00"), dd, "Landlord", "", true));
        }
    }

    public void addTransaction(Transaction t) {
        t.setId(UUID.randomUUID());
        if (t.getDate()==null) t.setDate(LocalDate.now());
        transactions.add(t);
    }
    public void addCategory(Category c) { c.setId(UUID.randomUUID()); categories.add(c); }
    public void addAccount(Account a) { a.setId(UUID.randomUUID()); accounts.add(a); }
    public void addBudget(Budget b) { b.setId(UUID.randomUUID()); budgets.add(b); }

    public DashboardVM buildDashboard() {
        YearMonth ym = YearMonth.now();
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        BigDecimal income = sumByTypeBetween(CategoryType.INCOME, start, end);
        BigDecimal expense = sumByTypeBetween(CategoryType.EXPENSE, start, end);

        DashboardVM vm = new DashboardVM();
        vm.mtdIncome = income;
        vm.mtdExpense = expense;
        vm.net = income.subtract(expense);

        // Spend by category (current month, expenses only)
        Map<String, BigDecimal> byCat = transactions.stream()
            .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
            .filter(t -> t.getCategory()!=null && t.getCategory().getType()==CategoryType.EXPENSE)
            .collect(Collectors.groupingBy(t -> t.getCategory().getName(),
                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));
        vm.categoryLabels = new ArrayList<>(byCat.keySet());
        vm.categoryValues = new ArrayList<>(byCat.values());

        // Cash flow last 6 months
        List<String> months = new ArrayList<>();
        List<BigDecimal> inc = new ArrayList<>();
        List<BigDecimal> exp = new ArrayList<>();
        for (int i=5;i>=0;i--) {
            YearMonth ymi = YearMonth.now().minusMonths(i);
            months.add(ymi.getMonth().toString().substring(0,3) + " " + (""+ymi.getYear()).substring(2));
            LocalDate s = ymi.atDay(1);
            LocalDate e = ymi.atEndOfMonth();
            inc.add(sumByTypeBetween(CategoryType.INCOME, s, e));
            exp.add(sumByTypeBetween(CategoryType.EXPENSE, s, e));
        }
        vm.months = months; vm.incomes = inc; vm.expenses = exp;

        // Recent 8 transactions
        vm.recentTransactions = transactions.stream()
            .sorted(Comparator.comparing(Transaction::getDate).reversed())
            .limit(8)
            .collect(Collectors.toList()); // compatible

        // Budgets usage (this month)
        List<BudgetUsage> usages = new ArrayList<>();
        for (Budget b : budgets.stream()
                .filter(bb -> bb.getMonth()==ym.getMonthValue() && bb.getYear()==ym.getYear())
                .collect(Collectors.toList())) { // compatible
            BigDecimal spent = transactions.stream()
                .filter(t -> t.getCategory()!=null && t.getCategory().getId().equals(b.getCategory().getId()))
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            usages.add(new BudgetUsage(b.getCategory().getName(), spent, b.getLimitAmount()));
        }
        vm.budgetUsages = usages;

        return vm;
    }

    private BigDecimal sumByTypeBetween(CategoryType type, LocalDate start, LocalDate end) {
        return transactions.stream()
            .filter(t -> t.getCategory()!=null && t.getCategory().getType()==type)
            .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
