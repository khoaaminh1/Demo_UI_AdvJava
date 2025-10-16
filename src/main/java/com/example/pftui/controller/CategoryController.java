package com.example.pftui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.pftui.model.Category;
import com.example.pftui.model.CategoryType;
import com.example.pftui.service.FakeDataService;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final FakeDataService data;
    public CategoryController(FakeDataService data) { this.data = data; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("active", "categories");
        model.addAttribute("categories", data.getCategories());
        return "categories";
    }

    @PostMapping
    public String create(@RequestParam String name, @RequestParam CategoryType type, @RequestParam(defaultValue="ri-price-tag-3-line") String icon) {
        data.addCategory(new Category(java.util.UUID.randomUUID(), name, type, icon));
        return "redirect:/categories";
    }
}
