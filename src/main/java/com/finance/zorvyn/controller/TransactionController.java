package com.finance.zorvyn.controller;

import com.finance.zorvyn.dto.request.TransactionRecordRequest;
import com.finance.zorvyn.dto.response.ApiResponse;
import com.finance.zorvyn.dto.response.TransactionRecordResponse;
import com.finance.zorvyn.entity.TransactionType;
import com.finance.zorvyn.entity.User;
import com.finance.zorvyn.service.TransactionService;
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

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Tag(name = "Transaction Records", description = "CRUD and filtering for financial transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {
    private final TransactionService recordService;

    /**
     * POST /api/records
     * Creates a new financial record. ADMIN only.
     *
     * The authenticated user's ID is extracted from the principal to stamp
     * the `created_by` field — we trust the server-side auth, not the request body.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a financial record — ADMIN only")
    public ResponseEntity<ApiResponse<TransactionRecordResponse>> createRecord(
            @Valid @RequestBody TransactionRecordRequest request,
            Authentication authentication) { // Spring injects current principal

        // Cast the principal to our User entity to get the user ID
        User currentUser = (User) authentication.getPrincipal();
        TransactionRecordResponse response = recordService.createRecord(request, currentUser.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED) // 201 Created for new resources
                .body(ApiResponse.success("Financial record created", response));
    }

    /**
     * GET /api/records?page=0&size=20&sort=transactionDate,desc
     * Returns all non-deleted records with pagination. ADMIN and ANALYST only.
     *
     * Pagination parameters:
     *   page    - zero-based page number (default 0)
     *   size    - records per page (default 20)
     *   sortBy  - field to sort by (default transactionDate)
     *   sortDir - asc or desc (default desc = newest first)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    @Operation(summary = "List all records (paginated) — ADMIN, ANALYST")
    public ResponseEntity<ApiResponse<Page<TransactionRecordResponse>>> getAllRecords(
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

    /**
     * GET /api/records/{id}
     * Returns a single record by its ID. ADMIN and ANALYST only.
     * Returns 404 if the record doesn't exist or is soft-deleted.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    @Operation(summary = "Get a single record by ID — ADMIN, ANALYST")
    public ResponseEntity<ApiResponse<TransactionRecordResponse>> getRecordById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success("Record retrieved", recordService.getRecordById(id)));
    }

    /**
     * PUT /api/records/{id}
     * Full update of a record — all fields replaced. ADMIN only.
     * Returns 404 if the record is not found or is soft-deleted.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a financial record — ADMIN only")
    public ResponseEntity<ApiResponse<TransactionRecordResponse>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRecordRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Record updated", recordService.updateRecord(id, request)));
    }

    /**
     * DELETE /api/records/{id}
     * Soft-deletes a record (sets deleted=true). ADMIN only.
     * Returns 204 No Content — successful deletion has no body.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a record — ADMIN only")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        // 204 No Content is the standard response for successful DELETE
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------
    // Filter endpoints — read-only, ADMIN and ANALYST
    // ---------------------------------------------------------------

    /**
     * GET /api/records/filter/type?type=INCOME&page=0&size=20
     * Filters records by transaction type (INCOME or EXPENSE).
     */
    @GetMapping("/filter/type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    @Operation(summary = "Filter records by type (INCOME/EXPENSE) — ADMIN, ANALYST")
    public ResponseEntity<ApiResponse<Page<TransactionRecordResponse>>> getByType(
            @RequestParam TransactionType type,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("transactionDate").descending());

        return ResponseEntity.ok(
                ApiResponse.success("Records retrieved",
                        recordService.getRecordsByType(type, pageable)));
    }

    /**
     * GET /api/records/filter/category?category=Groceries&page=0&size=20
     * Filters records by category name (case-insensitive match).
     */
    @GetMapping("/filter/category")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    @Operation(summary = "Filter records by category (case-insensitive) — ADMIN, ANALYST")
    public ResponseEntity<ApiResponse<Page<TransactionRecordResponse>>> getByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("transactionDate").descending());

        return ResponseEntity.ok(
                ApiResponse.success("Records retrieved",
                        recordService.getRecordsByCategory(category, pageable)));
    }

    /**
     * GET /api/records/filter/date-range?startDate=2024-01-01&endDate=2024-03-31
     * Filters records within an inclusive date range.
     *
     * @DateTimeFormat(iso = DATE) tells Spring how to parse the query param string
     * "2024-01-15" into a LocalDate object automatically.
     */
    @GetMapping("/filter/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    @Operation(summary = "Filter records by date range (ISO format: YYYY-MM-DD) — ADMIN, ANALYST")
    public ResponseEntity<ApiResponse<Page<TransactionRecordResponse>>> getByDateRange(
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
