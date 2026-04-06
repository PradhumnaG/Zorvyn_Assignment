package com.finance.zorvyn.service;

import com.finance.zorvyn.dto.request.FinancialRecordRequest;
import com.finance.zorvyn.dto.response.FinancialRecordResponse;
import com.finance.zorvyn.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
//finacailrecordservice  calls finacia record  fro crud and filtering
public interface FinancialRecordService {

    FinancialRecordResponse createRecord(FinancialRecordRequest request, Long userId);


    Page<FinancialRecordResponse> getAllRecords(Pageable pageable);


    FinancialRecordResponse getRecordById(Long id);


    FinancialRecordResponse updateRecord(Long id, FinancialRecordRequest request);


    void deleteRecord(Long id);


    Page<FinancialRecordResponse> getRecordsByType(TransactionType type, Pageable pageable);


    Page<FinancialRecordResponse> getRecordsByCategory(String category, Pageable pageable);


    Page<FinancialRecordResponse> getRecordsByDateRange(
            LocalDate startDate, LocalDate endDate, Pageable pageable);
}
