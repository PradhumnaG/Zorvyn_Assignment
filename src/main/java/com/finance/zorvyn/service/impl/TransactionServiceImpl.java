package com.finance.zorvyn.service.impl;

import com.finance.zorvyn.dto.request.TransactionRecordRequest;
import com.finance.zorvyn.dto.response.TransactionRecordResponse;
import com.finance.zorvyn.entity.TransactionRecord;
import com.finance.zorvyn.entity.TransactionType;
import com.finance.zorvyn.exception.ResourceNotFoundException;
import com.finance.zorvyn.repository.TransactionRepository;
import com.finance.zorvyn.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository recordRepository;

    // ---------------------------------------------------------------
    // Create
    // ---------------------------------------------------------------

    @Override
    @Transactional
    public TransactionRecordResponse createRecord(TransactionRecordRequest request, Long userId) {
        // Map the request DTO to an entity
        TransactionRecord record = TransactionRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim()) // Trim whitespace to normalise input
                .transactionDate(request.getTransactionDate())
                .notes(request.getNotes())
                .createdBy(userId) // Track which user created this record
                .deleted(false)
                .build();

        TransactionRecord saved = recordRepository.save(record);
        log.info("Financial record created: id={}, type={}, amount={}",
                saved.getId(), saved.getType(), saved.getAmount());
        return toResponse(saved);
    }

    // ---------------------------------------------------------------
    // Read
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionRecordResponse> getAllRecords(Pageable pageable) {
        // findByDeletedFalse automatically filters out soft-deleted rows
        return recordRepository.findByDeletedFalse(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionRecordResponse getRecordById(Long id) {
        return toResponse(findRecordOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionRecordResponse> getRecordsByType(
            TransactionType type, Pageable pageable) {
        return recordRepository.findByTypeAndDeletedFalse(type, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionRecordResponse> getRecordsByCategory(
            String category, Pageable pageable) {
        return recordRepository
                .findByCategoryIgnoreCaseAndDeletedFalse(category, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionRecordResponse> getRecordsByDateRange(
            LocalDate startDate, LocalDate endDate, Pageable pageable) {

        // Validate that start is not after end — the repository would silently return empty results
        if (startDate.isAfter(endDate)) {
            // Swap dates rather than throwing — more user-friendly for API consumers
            return recordRepository
                    .findByTransactionDateBetweenAndDeletedFalse(endDate, startDate, pageable)
                    .map(this::toResponse);
        }
        return recordRepository
                .findByTransactionDateBetweenAndDeletedFalse(startDate, endDate, pageable)
                .map(this::toResponse);
    }

    // ---------------------------------------------------------------
    // Update
    // ---------------------------------------------------------------

    /**
     * Full update: all fields from the request replace the existing values.
     * This is PUT semantics — the caller must provide all fields.
     */
    @Override
    @Transactional
    public TransactionRecordResponse updateRecord(Long id, TransactionRecordRequest request) {
        TransactionRecord record = findRecordOrThrow(id);

        // Overwrite all mutable fields
        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory().trim());
        record.setTransactionDate(request.getTransactionDate());
        record.setNotes(request.getNotes());
        // Note: createdBy and createdAt are intentionally NOT updated (audit integrity)

        TransactionRecord updated = recordRepository.save(record);
        log.info("Financial record updated: id={}", id);
        return toResponse(updated);
    }

    // ---------------------------------------------------------------
    // Delete (soft)
    // ---------------------------------------------------------------

    /**
     * Soft-deletes a record: sets deleted=true without removing the DB row.
     * The record will no longer appear in any query that filters deleted=false.
     */
    @Override
    @Transactional
    public void deleteRecord(Long id) {
        TransactionRecord record = findRecordOrThrow(id);
        record.setDeleted(true); // Mark as deleted — row stays in DB
        recordRepository.save(record);
        log.info("Financial record soft-deleted: id={}", id);
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    /** Loads a non-deleted record by ID or throws 404 */
    private TransactionRecord findRecordOrThrow(Long id) {
        return recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("TransactionRecord", "id", id));
    }

    /** Maps a TransactionRecord entity to a response DTO */
    private TransactionRecordResponse toResponse(TransactionRecord record) {
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
