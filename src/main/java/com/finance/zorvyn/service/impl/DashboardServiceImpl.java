package com.finance.zorvyn.service.impl;

import com.finance.zorvyn.repository.TransactionRepository;
import com.finance.zorvyn.dto.response.DashboardSummaryResponse;
import com.finance.zorvyn.dto.response.TransactionRecordResponse;
import com.finance.zorvyn.entity.TransactionRecord;
import com.finance.zorvyn.entity.TransactionType;
import com.finance.zorvyn.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository  transactionRepository;

    @Override
    @Transactional(readOnly = true) // No writes — readOnly avoids unnecessary locks
    public DashboardSummaryResponse getSummary(int trendMonths) {
        log.debug("Computing dashboard summary for last {} months", trendMonths);

        // -------------------------------------------------------
        // 1. Top-level monetary totals
        // -------------------------------------------------------

        // SUM(amount) WHERE type = INCOME AND deleted = false
        BigDecimal totalIncome = transactionRepository.sumAmountByType(TransactionType.INCOME);
        // SUM(amount) WHERE type = EXPENSE AND deleted = false
        BigDecimal totalExpenses = transactionRepository.sumAmountByType(TransactionType.EXPENSE);

        // Net balance: positive = net gain, negative = net loss
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        // Record counts
        long incomeCount = transactionRepository.countByTypeAndDeletedFalse(TransactionType.INCOME);
        long expenseCount = transactionRepository.countByTypeAndDeletedFalse(TransactionType.EXPENSE);

        // -------------------------------------------------------
        // 2. Category-wise breakdowns
        // -------------------------------------------------------

        // Returns List<Object[]> where each element is [String category, BigDecimal total]
        Map<String, BigDecimal> incomeByCategory =
                categoryMap(transactionRepository.sumAmountGroupedByCategory(TransactionType.INCOME));

        Map<String, BigDecimal> expenseByCategory =
                categoryMap(transactionRepository.sumAmountGroupedByCategory(TransactionType.EXPENSE));

        // -------------------------------------------------------
        // 3. Monthly trends (last N months)
        // -------------------------------------------------------

        // nativeQuery returns List<Object[]> where each element is [String month, BigDecimal total]
        List<DashboardSummaryResponse.TrendEntry> incomeTrend =
                trendList(transactionRepository.monthlyTrend("INCOME", trendMonths));

        List<DashboardSummaryResponse.TrendEntry> expenseTrend =
                trendList(transactionRepository.monthlyTrend("EXPENSE", trendMonths));

        // -------------------------------------------------------
        // 4. Recent activity (last 10 transactions)
        // -------------------------------------------------------

        List<TransactionRecordResponse> recentActivity =
                transactionRepository.findTop10ByDeletedFalseOrderByTransactionDateDesc()
                        .stream()
                        .map(this::toRecordResponse)
                        .collect(Collectors.toList());

        // -------------------------------------------------------
        // 5. Assemble and return
        // -------------------------------------------------------

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpenses)
                .totalBalance(netBalance)
                .totalIncomeRecords(incomeCount)
                .totalExpenseRecords(expenseCount)
                .incomeByCategory(incomeByCategory)
                .expenseByCategory(expenseByCategory)
                .incomeMonthlyTrend(incomeTrend)
                .expenseMonthlyTrend(expenseTrend)
                .recentActivity(recentActivity)
                .build();
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    /**
     * Converts a List<Object[]> of [category, total] pairs to a Map.
     * LinkedHashMap preserves the ORDER BY DESC from the query.
     */
    private Map<String, BigDecimal> categoryMap(List<Object[]> rows) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            BigDecimal total = (BigDecimal) row[1];
            map.put(category, total);
        }
        return map;
    }

    /**
     * Converts a List<Object[]> of [month, total] pairs to TrendEntry list.
     * month is a "YYYY-MM" string from DATE_FORMAT in the native query.
     */
    private List<DashboardSummaryResponse.TrendEntry> trendList(List<Object[]> rows) {
        return rows.stream()
                .map(row -> DashboardSummaryResponse.TrendEntry.builder()
                        .month((String) row[0])
                        .total(new BigDecimal(row[1].toString())) // Cast safely via String
                        .build())
                .collect(Collectors.toList());
    }

    /** Maps a FinancialRecord entity to FinancialRecordResponse DTO */
    private TransactionRecordResponse toRecordResponse(TransactionRecord record) {
        return TransactionRecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .transactionDate(record.getTransactionDate())
                .notes(record.getNotes())
                .createdBy(record.getCreatedBy())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
