package com.finance.zorvyn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal totalBalance;

    private  long totalIncomeRecords;
    private long totalExpenseRecords;

    private Map<String,BigDecimal> incomeByCategory;
    private Map<String, BigDecimal> expenseByCategory;
    private List<TrendEntry> incomeMonthlyTrend;
    private List<TrendEntry> expenseMonthlyTrend;
    private List<FinancialRecordResponse> recentActivity;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendEntry {
        private String month;
        private BigDecimal total;
    }
    }

