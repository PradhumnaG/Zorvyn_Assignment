package com.finance.zorvyn.dto.request;

import com.finance.zorvyn.entity.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
//creating and updating  finvaila record
@Data
public class FinancialRecordRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 13, fraction = 2,
            message = "Amount cannot exceed 13 integer digits and 2 decimal places")
    private BigDecimal amount;
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;
    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;


}
