package com.example.pftui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.pftui.model.Budget;
import com.example.pftui.service.FakeDataService;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Controller
@RequestMapping("/budgets")
public class BudgetController {
    private final FakeDataService data;
    public BudgetController(FakeDataService data) { this.data = data; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("active", "budgets");
        model.addAttribute("budgets", data.getBudgets());
        model.addAttribute("categories", data.getCategories());
        model.addAttribute("ym", YearMonth.now());
        model.addAttribute("usages", data.buildDashboard().budgetUsages);
        return "budgets";
    }

    @PostMapping
    public String create(@RequestParam UUID categoryId,
                         @RequestParam int month,
                         @RequestParam int year,
                         @RequestParam BigDecimal limit) {
        var c = data.getCategories().stream().filter(cc -> cc.getId().equals(categoryId)).findFirst().orElse(null);
        data.addBudget(new Budget(java.util.UUID.randomUUID(), c, month, year, limit));
        return "redirect:/budgets";
    }
}
