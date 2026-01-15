package com.autosavecoach.backend.controller;

import com.autosavecoach.backend.dto.DashboardResponse;
import com.autosavecoach.backend.service.DashboardService;
import com.autosavecoach.backend.util.MonthUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse getMonthlyDashboard(
            @RequestParam Long userId,
            @RequestParam String month
    ) {
        YearMonth yearMonth = MonthUtil.parse(month);
        return dashboardService.getDashboard(userId, yearMonth);
    }
}
