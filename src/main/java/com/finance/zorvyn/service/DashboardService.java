package com.finance.zorvyn.service;

import com.finance.zorvyn.dto.response.DashboardSummaryResponse;
//read only operation

public interface DashboardService {
    DashboardSummaryResponse getSummary(int trendMonths);
}
