package com.example.pftui.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.pftui.model.DashboardVM;
import com.example.pftui.security.CustomUserDetails;
import com.example.pftui.service.DashboardService;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    
    public DashboardController(DashboardService dashboardService) { 
        this.dashboardService = dashboardService; 
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get current logged in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        DashboardVM vm = new DashboardVM();
        
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            model.addAttribute("currentUser", userDetails.getUser());
            
            // Build dashboard with real data from database
            vm = dashboardService.buildDashboard(userDetails.getUser().getId());
        }
        
        model.addAttribute("active", "dashboard");
        model.addAttribute("vm", vm);
        return "dashboard";
    }
}
