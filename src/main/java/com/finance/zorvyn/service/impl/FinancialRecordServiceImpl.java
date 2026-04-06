package com.finance.zorvyn.service.impl;

import com.finance.zorvyn.dto.request.FinancialRecordRequest;
import com.finance.zorvyn.dto.response.FinancialRecordResponse;
import com.finance.zorvyn.entity.FinancialRecord;
import com.finance.zorvyn.entity.TransactionType;
import com.finance.zorvyn.exception.ResourceNotFoundException;
import com.finance.zorvyn.repository.FinancialRecordRepository;
import com.finance.zorvyn.service.FinancialRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
//CRUD ,filtering and soft-delete
@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialRecordServiceImpl implements FinancialRecordService {
    private final FinancialRecordRepository recordRepository;
    //create
    @Override
    @Transactional
    public FinancialRecordResponse createRecord(FinancialRecordRequest request, Long userId) {
        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim())
                .transactionDate(request.getTransactionDate())
                .notes(request.getNotes())
                .createdBy(userId) //created by
                .deleted(false)
                .build();

        FinancialRecord saved = recordRepository.save(record);
        log.info("Financial record created: id={}, type={}, amount={}",
                saved.getId(), saved.getType(), saved.getAmount());
        return toResponse(saved);
    }

  //read

    @Override
    @Transactional(readOnly = true)
    public Page<FinancialRecordResponse> getAllRecords(Pageable pageable) {
        return recordRepository.findByDeletedFalse(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialRecordResponse getRecordById(Long id) {
        return toResponse(findRecordOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FinancialRecordResponse> getRecordsByType(
            TransactionType type, Pageable pageable) {
        return recordRepository.findByTypeAndDeletedFalse(type, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FinancialRecordResponse> getRecordsByCategory(
            String category, Pageable pageable) {
        return recordRepository
                .findByCategoryIgnoreCaseAndDeletedFalse(category, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FinancialRecordResponse> getRecordsByDateRange(
            LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (startDate.isAfter(endDate)) {
            return recordRepository
                    .findByTransactionDateBetweenAndDeletedFalse(endDate, startDate, pageable)
                    .map(this::toResponse);
        }
        return recordRepository
                .findByTransactionDateBetweenAndDeletedFalse(startDate, endDate, pageable)
                .map(this::toResponse);
    }

    //update
    @Override
    @Transactional
    public FinancialRecordResponse updateRecord(Long id, FinancialRecordRequest request) {
        FinancialRecord record = findRecordOrThrow(id);


        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory().trim());
        record.setTransactionDate(request.getTransactionDate());
        record.setNotes(request.getNotes());
        FinancialRecord updated = recordRepository.save(record);
        log.info("Financial record updated: id={}", id);
        return toResponse(updated);
    }

//soft-delete
    @Override
    @Transactional
    public void deleteRecord(Long id) {
        FinancialRecord record = findRecordOrThrow(id);
        record.setDeleted(true); // soft-delete but stay in DB
        recordRepository.save(record);
        log.info("Financial record soft-deleted: id={}", id);
    }

//helper method
    private FinancialRecord findRecordOrThrow(Long id) {
        return recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("FinancialRecord", "id", id));
    }


    private FinancialRecordResponse toResponse(FinancialRecord record) {
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
