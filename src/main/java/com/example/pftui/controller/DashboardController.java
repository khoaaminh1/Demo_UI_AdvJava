package com.example.pftui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.pftui.service.FakeDataService;

@Controller
public class DashboardController {

    private final FakeDataService data;
    public DashboardController(FakeDataService data){ this.data = data; }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("active", "dashboard");
        model.addAttribute("vm", data.buildDashboard());
        return "dashboard";
    }
}
