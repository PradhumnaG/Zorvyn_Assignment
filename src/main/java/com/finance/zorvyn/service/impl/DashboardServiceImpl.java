package com.finance.zorvyn.service.impl;

import com.finance.zorvyn.repository.FinancialRecordRepository;
import com.finance.zorvyn.dto.response.DashboardSummaryResponse;
import com.finance.zorvyn.dto.response.FinancialRecordResponse;
import com.finance.zorvyn.entity.FinancialRecord;
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

    private final FinancialRecordRepository financialRecordRepository;

    @Override
    @Transactional(readOnly = true) // No writes — readOnly avoids unnecessary locks
    public DashboardSummaryResponse getSummary(int trendMonths) {
        log.debug("Computing dashboard summary for last {} months", trendMonths);

        //1
        BigDecimal totalIncome = financialRecordRepository.sumAmountByType(TransactionType.INCOME);
        BigDecimal totalExpenses = financialRecordRepository.sumAmountByType(TransactionType.EXPENSE);

        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        long incomeCount = financialRecordRepository.countByTypeAndDeletedFalse(TransactionType.INCOME);
        long expenseCount = financialRecordRepository.countByTypeAndDeletedFalse(TransactionType.EXPENSE);

       //2
        Map<String, BigDecimal> incomeByCategory =
                categoryMap(financialRecordRepository.sumAmountGroupedByCategory(TransactionType.INCOME));

        Map<String, BigDecimal> expenseByCategory =
                categoryMap(financialRecordRepository.sumAmountGroupedByCategory(TransactionType.EXPENSE));

        //3


        List<DashboardSummaryResponse.TrendEntry> incomeTrend =
                trendList(financialRecordRepository.monthlyTrend("INCOME", trendMonths));

        List<DashboardSummaryResponse.TrendEntry> expenseTrend =
                trendList(financialRecordRepository.monthlyTrend("EXPENSE", trendMonths));

         //4

        List<FinancialRecordResponse> recentActivity =
                financialRecordRepository.findTop10ByDeletedFalseOrderByTransactionDateDesc()
                        .stream()
                        .map(this::toRecordResponse)
                        .collect(Collectors.toList());

        //5
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

      //6 helper
    private Map<String, BigDecimal> categoryMap(List<Object[]> rows) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            BigDecimal total = (BigDecimal) row[1];
            map.put(category, total);
        }
        return map;
    }

    private List<DashboardSummaryResponse.TrendEntry> trendList(List<Object[]> rows) {
        return rows.stream()
                .map(row -> DashboardSummaryResponse.TrendEntry.builder()
                        .month((String) row[0])
                        .total(new BigDecimal(row[1].toString())) // Cast safely via String
                        .build())
                .collect(Collectors.toList());
    }

    private FinancialRecordResponse toRecordResponse(FinancialRecord record) {
        return FinancialRecordResponse.builder()
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
