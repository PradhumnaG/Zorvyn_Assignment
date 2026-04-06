package com.finance.zorvyn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.zorvyn.dto.request.FinancialRecordRequest;
import com.finance.zorvyn.dto.response.FinancialRecordResponse;
import com.finance.zorvyn.entity.Role;
import com.finance.zorvyn.entity.TransactionType;
import com.finance.zorvyn.entity.User;
import com.finance.zorvyn.service.FinancialRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FinancialRecordController.class)
class FinancialRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FinancialRecordService recordService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRecord_ShouldReturnCreated_WhenValidRequest() throws Exception {
        // Given
        FinancialRecordRequest request = FinancialRecordRequest.builder()
                .amount(BigDecimal.valueOf(100.00))
                .description("Test transaction")
                .category("Food")
                .transactionType(TransactionType.EXPENSE)
                .transactionDate(LocalDate.now())
                .build();

        FinancialRecordResponse response = FinancialRecordResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100.00))
                .description("Test transaction")
                .category("Food")
                .transactionType(TransactionType.EXPENSE)
                .transactionDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();

        when(recordService.createRecord(any(FinancialRecordRequest.class), anyLong())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amount").value(100.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllRecords_ShouldReturnPagedRecords_WhenAdmin() throws Exception {
        // Given
        FinancialRecordResponse record = FinancialRecordResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100.00))
                .description("Test transaction")
                .category("Food")
                .transactionType(TransactionType.EXPENSE)
                .transactionDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();

        Page<FinancialRecordResponse> page = new PageImpl<>(List.of(record), PageRequest.of(0, 20), 1);

        when(recordService.getAllRecords(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/records")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].description").value("Test transaction"));
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void getRecordById_ShouldReturnRecord_WhenAnalyst() throws Exception {
        // Given
        FinancialRecordResponse response = FinancialRecordResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100.00))
                .description("Test transaction")
                .category("Food")
                .transactionType(TransactionType.EXPENSE)
                .transactionDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();

        when(recordService.getRecordById(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/records/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRecord_ShouldReturnUpdatedRecord_WhenAdmin() throws Exception {
        // Given
        FinancialRecordRequest request = FinancialRecordRequest.builder()
                .amount(BigDecimal.valueOf(150.00))
                .description("Updated transaction")
                .category("Food")
                .transactionType(TransactionType.EXPENSE)
                .transactionDate(LocalDate.now())
                .build();

        FinancialRecordResponse updatedResponse = FinancialRecordResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(150.00))
                .description("Updated transaction")
                .category("Food")
                .transactionType(TransactionType.EXPENSE)
                .transactionDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();

        when(recordService.updateRecord(eq(1L), any(FinancialRecordRequest.class))).thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amount").value(150.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRecord_ShouldReturnNoContent_WhenAdmin() throws Exception {
        // Given
        doNothing().when(recordService).deleteRecord(1L);

        // When & Then
        mockMvc.perform(delete("/api/records/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void getByType_ShouldReturnPagedRecords_WhenAnalyst() throws Exception {
        // Given
        FinancialRecordResponse record = FinancialRecordResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100.00))
                .description("Test expense")
                .category("Food")
                .transactionType(TransactionType.EXPENSE)
                .transactionDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();

        Page<FinancialRecordResponse> page = new PageImpl<>(List.of(record), PageRequest.of(0, 20), 1);

        when(recordService.getRecordsByType(eq(TransactionType.EXPENSE), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/records/filter/type")
                        .param("type", "EXPENSE")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].transactionType").value("EXPENSE"));
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void getByCategory_ShouldReturnPagedRecords_WhenAnalyst() throws Exception {
        // Given
        FinancialRecordResponse record = FinancialRecordResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100.00))
                .description("Test transaction")
                .category("Food")
                .transactionType(TransactionType.EXPENSE)
                .transactionDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();

        Page<FinancialRecordResponse> page = new PageImpl<>(List.of(record), PageRequest.of(0, 20), 1);

        when(recordService.getRecordsByCategory(eq("Food"), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/records/filter/category")
                        .param("category", "Food")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].category").value("Food"));
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void getByDateRange_ShouldReturnPagedRecords_WhenAnalyst() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);

        FinancialRecordResponse record = FinancialRecordResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100.00))
                .description("Test transaction")
                .category("Food")
                .transactionType(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2023, 6, 15))
                .createdAt(LocalDateTime.now())
                .build();

        Page<FinancialRecordResponse> page = new PageImpl<>(List.of(record), PageRequest.of(0, 20), 1);

        when(recordService.getRecordsByDateRange(eq(startDate), eq(endDate), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/records/filter/date-range")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-12-31")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].transactionDate").value("2023-06-15"));
    }
}
