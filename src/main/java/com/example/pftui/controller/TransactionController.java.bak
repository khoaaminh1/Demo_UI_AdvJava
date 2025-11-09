package com.example.pftui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.pftui.model.*;
import com.example.pftui.service.FakeDataService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/transactions")
public class TransactionController {
    private final FakeDataService data;
    public TransactionController(FakeDataService data) { this.data = data; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("active", "transactions");
        model.addAttribute("transactions", data.getTransactions());
        model.addAttribute("categories", data.getCategories());
        model.addAttribute("accounts", data.getAccounts());
        return "transactions";
    }

    @PostMapping
    public String create(@RequestParam UUID accountId,
                         @RequestParam UUID categoryId,
                         @RequestParam BigDecimal amount,
                         @RequestParam String merchant,
                         @RequestParam(required=false) String note,
                         @RequestParam String date) {
        Account acc = data.getAccounts().stream().filter(a -> a.getId().equals(accountId)).findFirst().orElse(null);
        Category cat = data.getCategories().stream().filter(c -> c.getId().equals(categoryId)).findFirst().orElse(null);
        Transaction t = new Transaction(null, acc, cat, amount, LocalDate.parse(date), merchant, note, false);
        data.addTransaction(t);
        return "redirect:/transactions";
    }
}
