package com.finance.zorvyn.repository;

import com.finance.zorvyn.entity.FinancialRecord;
import com.finance.zorvyn.entity.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class FinancialRecordRepositoryTest {

    @Autowired
    private FinancialRecordRepository repository;

    @Test
    void saveAndQueryMethods() {
        FinancialRecord r = FinancialRecord.builder()
                .amount(BigDecimal.valueOf(10))
                .type(TransactionType.EXPENSE)
                .category("Food")
                .transactionDate(LocalDate.now())
                .createdBy(1L)
                .build();

        repository.save(r);

        var page = repository.findByDeletedFalse(PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());

        var byType = repository.findByTypeAndDeletedFalse(TransactionType.EXPENSE, PageRequest.of(0, 10));
        assertEquals(1, byType.getTotalElements());
    }
}

