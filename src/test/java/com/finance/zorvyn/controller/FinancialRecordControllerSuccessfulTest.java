package com.finance.zorvyn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.zorvyn.dto.request.FinancialRecordRequest;
import com.finance.zorvyn.dto.response.FinancialRecordResponse;
import com.finance.zorvyn.entity.TransactionType;
import com.finance.zorvyn.entity.User;
import com.finance.zorvyn.entity.Role;
import com.finance.zorvyn.service.FinancialRecordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.http.HttpStatus;
import com.finance.zorvyn.dto.response.ApiResponse;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(FinancialRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FinancialRecordController - happy path tests")
public class FinancialRecordControllerSuccessfulTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FinancialRecordService recordService;

    @MockBean
    private com.finance.zorvyn.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    // create record as ADMIN
    @Test
    @DisplayName("POST /api/records - create record (ADMIN)")
    void createRecord_admin_success() throws Exception {
        FinancialRecordRequest req = new FinancialRecordRequest();
        req.setAmount(BigDecimal.valueOf(100.00));
        req.setType(TransactionType.EXPENSE);
        req.setCategory("Food");
        req.setTransactionDate(LocalDate.now());

        FinancialRecordResponse resp = FinancialRecordResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionType.EXPENSE)
                .category("Food")
                .transactionDate(LocalDate.now())
                .notes("Test")
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();

        // mock service
        when(recordService.createRecord(any(FinancialRecordRequest.class), eq(1L))).thenReturn(resp);

        // create a User principal with ADMIN role
        User principal = User.builder()
                .id(1L)
                .name("Admin")
                .email("admin@t.com")
                .password("x")
                .role(Role.ADMIN)
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        // Call controller directly to avoid Security/MockMvc complexity for principal injection
        FinancialRecordController controller = new FinancialRecordController(recordService);
        var response = controller.createRecord(req, auth);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ApiResponse<FinancialRecordResponse> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertNotNull(body.getData());
        assertEquals(1L, body.getData().getId());
    }

    @Test
    @DisplayName("GET /api/records - list records (ADMIN)")
    void getAllRecords_admin_success() throws Exception {
        FinancialRecordResponse resp = FinancialRecordResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionType.EXPENSE)
                .category("Food")
                .transactionDate(LocalDate.now())
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();

        when(recordService.getAllRecords(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(resp), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/records")
                        .param("page", "0")
                        .param("size", "20")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }
}

