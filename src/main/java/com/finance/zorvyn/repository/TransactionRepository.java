package com.finance.zorvyn.repository;

import com.finance.zorvyn.entity.TransactionRecord;
import com.finance.zorvyn.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionRecord, Long>,
        JpaSpecificationExecutor<TransactionRecord> {

    Optional<TransactionRecord> findByIdAndDeletedFalse(Long id);


    Page<TransactionRecord> findByDeletedFalse(Pageable pageable);


    Page<TransactionRecord> findByTypeAndDeletedFalse(TransactionType type, Pageable pageable);


    Page<TransactionRecord> findByCategoryIgnoreCaseAndDeletedFalse(String category, Pageable pageable);


    Page<TransactionRecord> findByTransactionDateBetweenAndDeletedFalse(
            LocalDate startDate, LocalDate endDate, Pageable pageable);




    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM TransactionRecord r " +
            "WHERE r.type = :type AND r.deleted = false")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);


    @Query("SELECT r.category, COALESCE(SUM(r.amount), 0) FROM TransactionRecord r " +
            "WHERE r.type = :type AND r.deleted = false " +
            "GROUP BY r.category ORDER BY SUM(r.amount) DESC")
    List<Object[]> sumAmountGroupedByCategory(@Param("type") TransactionType type);


    @Query(value = "SELECT DATE_FORMAT(transaction_date, '%Y-%m') AS month, " +
            "COALESCE(SUM(amount), 0) AS total " +
            "FROM transaction_record " +
            "WHERE type = :type AND deleted = false " +
            "AND transaction_date >= DATE_SUB(CURDATE(), INTERVAL :months MONTH) " +
            "GROUP BY month ORDER BY month ASC",
            nativeQuery = true) // nativeQuery = true means raw SQL, not JPQL
    List<Object[]> monthlyTrend(@Param("type") String type,
                                @Param("months") int months);


    List<TransactionRecord> findTop10ByDeletedFalseOrderByTransactionDateDesc();


    long countByTypeAndDeletedFalse(TransactionType type);
}
