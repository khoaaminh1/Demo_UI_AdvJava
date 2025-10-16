package com.example.pftui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.pftui.model.*;
import com.example.pftui.service.FakeDataService;

@Controller
@RequestMapping("/accounts")
public class AccountController {
    private final FakeDataService data;
    public AccountController(FakeDataService data) { this.data = data; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("active", "accounts");
        model.addAttribute("accounts", data.getAccounts());
        return "accounts";
    }

    @PostMapping
    public String create(@RequestParam String name, @RequestParam AccountType type, @RequestParam(defaultValue="USD") String currency) {
        data.addAccount(new Account(java.util.UUID.randomUUID(), name, type, currency));
        return "redirect:/accounts";
    }
}
