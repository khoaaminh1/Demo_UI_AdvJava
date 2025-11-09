package com.example.pftui.controller;

import com.example.pftui.model.Category;
import com.example.pftui.model.CategoryType;
import com.example.pftui.security.CustomUserDetails;
import com.example.pftui.service.CategoryService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(@RequestParam(required = false) String editId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String userId = userDetails.getUser().getId();

        List<Category> allCategories = categoryService.getAllCategoriesForUser(userId);
        
        List<Category> incomeCategories = allCategories.stream()
            .filter(c -> c.getType() == CategoryType.INCOME)
            .collect(Collectors.toList());

        List<Category> expenseCategories = allCategories.stream()
            .filter(c -> c.getType() == CategoryType.EXPENSE)
            .collect(Collectors.toList());

        model.addAttribute("active", "categories");
        model.addAttribute("currentUser", userDetails.getUser());
        model.addAttribute("incomeCategories", incomeCategories);
        model.addAttribute("expenseCategories", expenseCategories);
        model.addAttribute("incomeCount", incomeCategories.size());
        model.addAttribute("expenseCount", expenseCategories.size());

        if (editId != null) {
            categoryService.getCategoryById(editId).ifPresent(category -> {
                // Ensure user can only edit their own non-system categories
                if (!category.isSystem() && userId.equals(category.getUserId())) {
                    model.addAttribute("editCategory", category);
                }
            });
        }

        return "categories";
    }

    @PostMapping
    public String saveCategory(@RequestParam(required = false) String id,
                             @RequestParam String name,
                             @RequestParam CategoryType type,
                             @RequestParam(defaultValue = "ri-price-tag-3-line") String icon,
                             RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String userId = userDetails.getUser().getId();

        try {
            Category category = (id != null && !id.isEmpty()) 
                ? categoryService.getCategoryById(id).orElse(new Category()) 
                : new Category();

            // Security check: ensure user is not updating a category they don't own or a system category
            if (id != null && !id.isEmpty() && (category.isSystem() || !userId.equals(category.getUserId()))) {
                 throw new SecurityException("You do not have permission to edit this category.");
            }

            category.setUserId(userId);
            category.setName(name);
            category.setType(type);
            category.setIcon(icon);
            category.setSystem(false); // User-created categories are never system categories

            categoryService.createCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Category saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving category: " + e.getMessage());
        }

        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String userId = userDetails.getUser().getId();

        try {
            Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

            // Security check: User can only delete their own, non-system categories
            if (category.isSystem() || !userId.equals(category.getUserId())) {
                throw new SecurityException("You do not have permission to delete this category.");
            }

            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting category: " + e.getMessage());
        }

        return "redirect:/categories";
    }
}
