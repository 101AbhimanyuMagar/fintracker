package com.fintracker_backend.fintracker.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fintracker_backend.fintracker.entity.Transaction;
import com.fintracker_backend.fintracker.entity.TransactionType;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByUserIdAndType(Long userId, TransactionType type);

    List<Transaction> findByAccountId(Long accountId);

    List<Transaction> findByCategoryId(Long categoryId);

    List<Transaction> findByUserIdAndCreatedAtBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end);

    @Query("""
    SELECT SUM(t.amount)
    FROM Transaction t
    WHERE t.user.id = :userId
      AND t.category.id = :categoryId
      AND t.type = 'EXPENSE'
      AND FUNCTION('DATE_FORMAT', t.createdAt, '%Y-%m') = :period
""")
BigDecimal getTotalExpenseByCategoryAndPeriod(
        @Param("userId") Long userId,
        @Param("categoryId") Long categoryId,
        @Param("period") String period
);
}
