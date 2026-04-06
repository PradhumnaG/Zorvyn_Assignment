package com.finance.zorvyn.controller;

import com.finance.zorvyn.dto.response.DashboardSummaryResponse;
import com.finance.zorvyn.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    @WithMockUser
    void getSummary_ShouldReturnDashboardSummary_WhenAuthenticated() throws Exception {
        // Given
        DashboardSummaryResponse summary = DashboardSummaryResponse.builder()
                .totalIncome(BigDecimal.valueOf(10000.00))
                .totalExpense(BigDecimal.valueOf(5000.00))
                .netBalance(BigDecimal.valueOf(5000.00))
                .totalTransactions(150L)
                .categoryBreakdown(Map.of("Food", BigDecimal.valueOf(2000.00)))
                .monthlyTrends(Map.of())
                .recentActivity(List.of())
                .build();

        when(dashboardService.getSummary(anyInt())).thenReturn(summary);

        // When & Then
        mockMvc.perform(get("/api/dashboard/summary")
                        .param("months", "6"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalIncome").value(10000.00))
                .andExpect(jsonPath("$.data.totalExpense").value(5000.00))
                .andExpect(jsonPath("$.data.netBalance").value(5000.00));
    }

    @Test
    @WithMockUser
    void getSummary_ShouldClampMonths_WhenOutOfRange() throws Exception {
        // Given
        DashboardSummaryResponse summary = DashboardSummaryResponse.builder()
                .totalIncome(BigDecimal.valueOf(10000.00))
                .totalExpense(BigDecimal.valueOf(5000.00))
                .netBalance(BigDecimal.valueOf(5000.00))
                .totalTransactions(150L)
                .categoryBreakdown(Map.of())
                .monthlyTrends(Map.of())
                .recentActivity(List.of())
                .build();

        when(dashboardService.getSummary(24)).thenReturn(summary); // Should clamp to 24

        // When & Then
        mockMvc.perform(get("/api/dashboard/summary")
                        .param("months", "30")) // Out of range
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));
    }
}
