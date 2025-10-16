package com.example.pftui.model;

import java.math.BigDecimal;
import java.util.List;

public class DashboardVM {
    public BigDecimal mtdIncome;
    public BigDecimal mtdExpense;
    public BigDecimal net;

    public List<String> categoryLabels;
    public List<BigDecimal> categoryValues;

    public List<String> months;
    public List<BigDecimal> incomes;
    public List<BigDecimal> expenses;

    public List<Transaction> recentTransactions;
    public List<BudgetUsage> budgetUsages;
}
