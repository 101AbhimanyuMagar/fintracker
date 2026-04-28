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

// 📊 Monthly Expense Trend
@Query("""
    SELECT FUNCTION('DATE_FORMAT', t.createdAt, '%Y-%m'), SUM(t.amount)
    FROM Transaction t
    WHERE t.user.id = :userId
      AND t.type = 'EXPENSE'
    GROUP BY FUNCTION('DATE_FORMAT', t.createdAt, '%Y-%m')
    ORDER BY FUNCTION('DATE_FORMAT', t.createdAt, '%Y-%m')
""")
List<Object[]> getMonthlyExpenseTrend(@Param("userId") Long userId);



@Query("""
SELECT FUNCTION('DATE', t.createdAt), SUM(t.amount)
FROM Transaction t
WHERE t.user.id = :userId
  AND t.type = 'EXPENSE'
  AND FUNCTION('YEAR', t.createdAt) = :year
  AND FUNCTION('MONTH', t.createdAt) = :month
GROUP BY FUNCTION('DATE', t.createdAt)
ORDER BY FUNCTION('DATE', t.createdAt)
""")
List<Object[]> getDailyExpense(
        @Param("userId") Long userId,
        @Param("year") int year,
        @Param("month") int month
);

@Query("""
SELECT t.category.name, SUM(t.amount)
FROM Transaction t
WHERE t.user.id = :userId
  AND t.type = 'EXPENSE'
  AND FUNCTION('YEAR', t.createdAt) = :year
  AND FUNCTION('MONTH', t.createdAt) = :month
GROUP BY t.category.name
ORDER BY SUM(t.amount) DESC
""")
List<Object[]> getTopCategories(
        @Param("userId") Long userId,
        @Param("year") int year,
        @Param("month") int month
);


   @Query("""
    SELECT SUM(t.amount)
    FROM Transaction t
    WHERE t.user.id = :userId
      AND t.type = 'INCOME'
      AND FUNCTION('DATE_FORMAT', t.createdAt, '%Y-%m') = :period
""")

BigDecimal getTotalExpenseByMonth(
        @Param("userId") Long userId,
        @Param("period") String period
);

// 🧾 Category-wise Expense
@Query("""
    SELECT t.category.name, SUM(t.amount)
    FROM Transaction t
    WHERE t.user.id = :userId
      AND t.type = 'EXPENSE'
    GROUP BY t.category.name
""")
List<Object[]> getCategoryExpense(@Param("userId") Long userId);

    @Query("""
        SELECT t.category.name, SUM(t.amount)
        FROM Transaction t
        WHERE t.user.id = :userId
          AND t.type = 'EXPENSE'
          AND FUNCTION('DATE_FORMAT', t.createdAt, '%Y-%m') = :period
        GROUP BY t.category.name
    """)
    List<Object[]> getCategoryWiseExpense(@Param("userId") Long userId, @Param("period") String period);

    @Query("""
        SELECT SUM(t.amount)
        FROM Transaction t
        WHERE t.user.id = :userId
          AND t.type = 'INCOME'
          AND FUNCTION('DATE_FORMAT', t.createdAt, '%Y-%m') = :period
    """)
    BigDecimal getTotalIncomeByMonth(@Param("userId") Long userId, @Param("period") String period);


// 📈 Income vs Expense
@Query("""
    SELECT t.type, SUM(t.amount)
    FROM Transaction t
    WHERE t.user.id = :userId
    GROUP BY t.type
""")
List<Object[]> getIncomeVsExpense(@Param("userId") Long userId);


    @Query("""
    SELECT a.name, SUM(t.amount)
    FROM Transaction t
    JOIN t.account a
    WHERE t.user.id = :userId
      AND t.type = 'EXPENSE'
    GROUP BY a.name
""")
List<Object[]> getExpenseByAccount(@Param("userId") Long userId);
}
