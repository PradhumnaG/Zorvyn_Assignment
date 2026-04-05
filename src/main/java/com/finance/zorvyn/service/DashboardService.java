package com.finance.zorvyn.service;

import com.finance.zorvyn.dto.response.DashboardSummaryResponse;


public interface DashboardService {
    DashboardSummaryResponse getSummary(int trendMonths);
}
