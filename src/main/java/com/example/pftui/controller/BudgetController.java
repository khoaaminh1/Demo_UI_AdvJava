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
    public String list(@RequestParam(required = false) Integer month,
                      @RequestParam(required = false) Integer year,
                      @RequestParam(required = false) UUID editId,
                      Model model) {
        // Determine which month/year to display
        YearMonth ym;
        if (month != null && year != null) {
            ym = YearMonth.of(year, month);
        } else {
            ym = YearMonth.now();
        }
        
        model.addAttribute("active", "budgets");
        model.addAttribute("budgets", data.getBudgets());
        model.addAttribute("categories", data.getCategories());
        model.addAttribute("ym", ym);
        model.addAttribute("selectedMonth", ym.getMonthValue());
        model.addAttribute("selectedYear", ym.getYear());
        
        // Build budget usages for the selected month
        var usages = data.buildBudgetUsagesForMonth(ym);
        model.addAttribute("usages", usages);
        
        // If editing, add the budget to edit
        if (editId != null) {
            var budgetToEdit = data.getBudgets().stream()
                .filter(b -> b.getId().equals(editId))
                .findFirst()
                .orElse(null);
            model.addAttribute("editBudget", budgetToEdit);
        }
        
        return "budgets";
    }

    @PostMapping
    public String create(@RequestParam UUID categoryId,
                         @RequestParam int month,
                         @RequestParam int year,
                         @RequestParam BigDecimal limit,
                         @RequestParam(required = false) Boolean applyToAllMonths) {
        var c = data.getCategories().stream()
            .filter(cc -> cc.getId().equals(categoryId))
            .findFirst()
            .orElse(null);
        
        if (applyToAllMonths != null && applyToAllMonths) {
            // Create budget for all 12 months
            for (int m = 1; m <= 12; m++) {
                data.addBudget(new Budget(UUID.randomUUID(), c, m, year, limit));
            }
        } else {
            // Create budget for single month
            data.addBudget(new Budget(UUID.randomUUID(), c, month, year, limit));
        }
        
        return "redirect:/budgets?month=" + month + "&year=" + year;
    }
    
    @PostMapping("/{id}/update")
    public String update(@PathVariable UUID id,
                        @RequestParam UUID categoryId,
                        @RequestParam int month,
                        @RequestParam int year,
                        @RequestParam BigDecimal limit) {
        var budget = data.getBudgets().stream()
            .filter(b -> b.getId().equals(id))
            .findFirst()
            .orElse(null);
            
        if (budget != null) {
            var c = data.getCategories().stream()
                .filter(cc -> cc.getId().equals(categoryId))
                .findFirst()
                .orElse(null);
                
            budget.setCategory(c);
            budget.setMonth(month);
            budget.setYear(year);
            budget.setLimitAmount(limit);
        }
        
        return "redirect:/budgets?month=" + month + "&year=" + year;
    }
    
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id,
                        @RequestParam(required = false) Integer month,
                        @RequestParam(required = false) Integer year) {
        data.getBudgets().removeIf(b -> b.getId().equals(id));
        
        String redirect = "redirect:/budgets";
        if (month != null && year != null) {
            redirect += "?month=" + month + "&year=" + year;
        }
        return redirect;
    }
}
