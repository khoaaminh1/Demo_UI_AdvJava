package com.example.pftui.controller;

import com.example.pftui.model.Budget;
import com.example.pftui.model.CategoryType;
import com.example.pftui.security.CustomUserDetails;
import com.example.pftui.service.BudgetService;
import com.example.pftui.service.CategoryService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final CategoryService categoryService;

    public BudgetController(BudgetService budgetService, CategoryService categoryService) {
        this.budgetService = budgetService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listBudgets(@RequestParam(required = false) Integer month,
                              @RequestParam(required = false) Integer year,
                              @RequestParam(required = false) String editId,
                              Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String userId = userDetails.getUser().getId();

        YearMonth ym = (month != null && year != null) ? YearMonth.of(year, month) : YearMonth.now();

        model.addAttribute("active", "budgets");
        model.addAttribute("currentUser", userDetails.getUser());
        model.addAttribute("ym", ym);
        model.addAttribute("selectedMonth", ym.getMonthValue());
        model.addAttribute("selectedYear", ym.getYear());
        
        // Get budget usages for the selected month
        model.addAttribute("budgetUsages", budgetService.getBudgetUsages(userId, ym.getMonthValue(), ym.getYear()));

        // Get budgets for the selected month and year
        model.addAttribute("budgets", budgetService.getBudgetsForUser(userId, ym.getMonthValue(), ym.getYear()));
        
        // Get only EXPENSE categories for the dropdown
        model.addAttribute("categories", categoryService.getAllCategoriesForUser(userId).stream()
            .filter(c -> c.getType() == CategoryType.EXPENSE).collect(Collectors.toList()));
        
        // If editing, fetch the budget and add it to the model
        if (editId != null) {
            budgetService.getBudgetById(editId)
                .ifPresent(budget -> model.addAttribute("editBudget", budget));
        }

        return "budgets";
    }

    @PostMapping
    public String saveBudget(@RequestParam(required = false) String id,
                           @RequestParam String categoryId,
                           @RequestParam int month,
                           @RequestParam int year,
                           @RequestParam BigDecimal limitAmount,
                           RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String userId = userDetails.getUser().getId();

        try {
            Budget budget = (id != null && !id.isEmpty()) 
                ? budgetService.getBudgetById(id).orElse(new Budget()) 
                : new Budget();
            
            budget.setUserId(userId);
            budget.setCategoryId(categoryId);
            budget.setMonth(month);
            budget.setYear(year);
            budget.setLimitAmount(limitAmount);

            budgetService.saveBudget(budget);
            redirectAttributes.addFlashAttribute("successMessage", "Budget saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving budget: " + e.getMessage());
        }

        return "redirect:/budgets?month=" + month + "&year=" + year;
    }

    @PostMapping("/{id}/delete")
    public String deleteBudget(@PathVariable String id,
                             @RequestParam int month,
                             @RequestParam int year,
                             RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String userId = userDetails.getUser().getId();

        try {
            budgetService.deleteBudget(id, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Budget deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting budget: " + e.getMessage());
        }

        return "redirect:/budgets?month=" + month + "&year=" + year;
    }
}
