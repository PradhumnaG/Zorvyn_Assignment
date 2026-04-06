package com.finance.zorvyn.controller;

import com.finance.zorvyn.dto.request.FinancialRecordRequest;
import com.finance.zorvyn.dto.response.ApiResponse;
import com.finance.zorvyn.dto.response.FinancialRecordResponse;
import com.finance.zorvyn.entity.TransactionType;
import com.finance.zorvyn.entity.User;
import com.finance.zorvyn.service.FinancialRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
//crud for financial record
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Tag(name = "Transaction Records", description = "CRUD and filtering for financial transactions")
@SecurityRequirement(name = "bearerAuth")
public class FinancialRecordController {
    private final FinancialRecordService recordService;

//create record
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a financial record — ADMIN only")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> createRecord(
            @Valid @RequestBody FinancialRecordRequest request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        FinancialRecordResponse response = recordService.createRecord(request, currentUser.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED) // 201 Created for new resources
                .body(ApiResponse.success("Financial record created", response));
    }

//admin and analyst  return all non deleted record with pagination
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    @Operation(summary = "List all records (paginated) — ADMIN, ANALYST")
    public ResponseEntity<ApiResponse<Page<FinancialRecordResponse>>> getAllRecords(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                ApiResponse.success("Records retrieved", recordService.getAllRecords(pageable)));
    }

    //return single record by id
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    @Operation(summary = "Get a single record by ID — ADMIN, ANALYST")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> getRecordById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success("Record retrieved", recordService.getRecordById(id)));
    }

   //full update of recod
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a financial record — ADMIN only")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody FinancialRecordRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Record updated", recordService.updateRecord(id, request)));
    }

    //soft delete record
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a record — ADMIN only")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);

        return ResponseEntity.noContent().build();
    }

   //filler record by transaction type
    @GetMapping("/filter/type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    @Operation(summary = "Filter records by type (INCOME/EXPENSE) — ADMIN, ANALYST")
    public ResponseEntity<ApiResponse<Page<FinancialRecordResponse>>> getByType(
            @RequestParam TransactionType type,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("transactionDate").descending());

        return ResponseEntity.ok(
                ApiResponse.success("Records retrieved",
                        recordService.getRecordsByType(type, pageable)));
    }

   //filter record by category
    @GetMapping("/filter/category")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    @Operation(summary = "Filter records by category (case-insensitive) — ADMIN, ANALYST")
    public ResponseEntity<ApiResponse<Page<FinancialRecordResponse>>> getByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("transactionDate").descending());

        return ResponseEntity.ok(
                ApiResponse.success("Records retrieved",
                        recordService.getRecordsByCategory(category, pageable)));
    }

   //filter record by date range
    @GetMapping("/filter/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    @Operation(summary = "Filter records by date range (ISO format: YYYY-MM-DD) — ADMIN, ANALYST")
    public ResponseEntity<ApiResponse<Page<FinancialRecordResponse>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("transactionDate").descending());

        return ResponseEntity.ok(
                ApiResponse.success("Records retrieved",
                        recordService.getRecordsByDateRange(startDate, endDate, pageable)));
    }

}
