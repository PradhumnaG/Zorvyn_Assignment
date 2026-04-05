package com.finance.zorvyn.service;

import com.finance.zorvyn.dto.request.TransactionRecordRequest;
import com.finance.zorvyn.dto.response.TransactionRecordResponse;
import com.finance.zorvyn.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface TransactionService {

    TransactionRecordResponse createRecord(TransactionRecordRequest request, Long userId);


    Page<TransactionRecordResponse> getAllRecords(Pageable pageable);


    TransactionRecordResponse  getRecordById(Long id);


    TransactionRecordResponse  updateRecord(Long id, TransactionRecordRequest request);


    void deleteRecord(Long id);


    Page<TransactionRecordResponse > getRecordsByType(TransactionType type, Pageable pageable);


    Page<TransactionRecordResponse > getRecordsByCategory(String category, Pageable pageable);


    Page<TransactionRecordResponse > getRecordsByDateRange(
            LocalDate startDate, LocalDate endDate, Pageable pageable);
}
