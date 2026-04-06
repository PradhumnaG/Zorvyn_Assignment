package com.finance.zorvyn.controller;

import com.finance.zorvyn.dto.response.ApiResponse;
import com.finance.zorvyn.dto.response.DashboardSummaryResponse;
import com.finance.zorvyn.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
//dashboard summary
//all can access
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregated financial analytics — all authenticated roles")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {
    private final DashboardService dashboardService;

//summary of all record
    //all can access
    @GetMapping("/summary")
    @Operation(
            summary = "Get full dashboard summary — all roles",
            description = "Returns totals, category breakdowns, monthly trends, and recent activity. " +
                    "Use the `months` parameter to control how many months of trend data are returned."
    )
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @RequestParam(defaultValue = "6") int months) {

        int clampedMonths = Math.max(1, Math.min(months, 24));

        DashboardSummaryResponse summary = dashboardService.getSummary(clampedMonths);
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved", summary));
    }
}
