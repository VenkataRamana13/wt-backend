package com.wtplatform.backend.repository;

import com.wtplatform.backend.model.Transaction;
import com.wtplatform.backend.dto.StpTrendDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * Find all transactions for a specific client
     */
    List<Transaction> findByClientId(Long clientId);
    
    /**
     * Find all transactions for a specific client with pagination
     */
    Page<Transaction> findByClientId(Long clientId, Pageable pageable);
    
    /**
     * Find all transactions for a specific client and transaction type
     */
    List<Transaction> findByClientIdAndType(Long clientId, String type);
    
    /**
     * Find all transactions for a specific client within a date range
     */
    List<Transaction> findByClientIdAndTransactionDateBetween(Long clientId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find all transactions for clients belonging to a specific user
     */
    @Query("SELECT t FROM Transaction t JOIN t.client c WHERE c.user.id = :userId")
    List<Transaction> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find all transactions for clients belonging to a specific user with pagination
     */
    @Query("SELECT t FROM Transaction t JOIN t.client c WHERE c.user.id = :userId")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find transactions for clients belonging to a specific user ordered by date (descending)
     */
    @Query("SELECT t FROM Transaction t JOIN t.client c WHERE c.user.id = :userId ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdOrderByTransactionDateDesc(@Param("userId") Long userId);
    
    /**
     * Find limited number of transactions for clients belonging to a specific user ordered by date (descending)
     */
    @Query("SELECT t FROM Transaction t JOIN t.client c WHERE c.user.id = :userId ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdOrderByTransactionDateDesc(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find all transactions with a specific status
     */
    List<Transaction> findByStatus(String status);
    
    /**
     * Find all transactions with a specific type
     */
    List<Transaction> findByType(String type);
    
    /**
     * Find all transactions within a date range
     */
    List<Transaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find all transactions for a specific client with a specific status
     */
    List<Transaction> findByClientIdAndStatus(Long clientId, String status);
    
    /**
     * Count all transactions for a specific client
     */
    long countByClientId(Long clientId);
    
    /**
     * Sum the amount of all transactions for a specific client
     */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.client.id = :clientId")
    BigDecimal sumAmountByClientId(@Param("clientId") Long clientId);
    
    /**
     * Sum the amount of all transactions for a specific client and transaction type
     */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.client.id = :clientId AND t.type = :type")
    BigDecimal sumAmountByClientIdAndType(@Param("clientId") Long clientId, @Param("type") String type);

    @Query("SELECT COUNT(t) FROM Transaction t " +
           "WHERE t.client.user.id = :userId " +
           "AND t.type = 'STP' " +
           "AND t.status = 'ACTIVE' " +
           "AND t.endDate > CURRENT_DATE")
    Long countActiveStpsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Transaction t " +
           "WHERE t.client.user.id = :userId " +
           "AND t.type = 'STP' " +
           "AND t.nextTransactionDate = :today " +
           "AND t.status = 'ACTIVE'")
    Long countStpsExecutingToday(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("SELECT COUNT(t) FROM Transaction t " +
           "WHERE t.client.user.id = :userId " +
           "AND t.type = 'STP' " +
           "AND t.endDate BETWEEN :startDate AND :endDate " +
           "AND t.status = 'ACTIVE'")
    Long countStpsExpiringBetween(@Param("userId") Long userId, 
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(t) FROM Transaction t " +
           "WHERE t.client.user.id = :userId " +
           "AND t.type = 'STP' " +
           "AND t.status = 'ACTIVE' " +
           "AND NOT EXISTS (SELECT 1 FROM FundBalance fb " +
           "               WHERE fb.fundId = t.fromFund " +
           "               AND fb.balance >= t.amount)")
    Long countStpsWithZeroBalance(@Param("userId") Long userId);

    // Commented out problematic JPQL query
    /*
    @Query("SELECT new com.wtplatform.backend.dto.StpTrendDTO(" +
           "    FUNCTION('DATE_FORMAT', t.transactionDate, '%Y-%m') as month, " +
           "    CAST(SUM(t.amount) AS big_decimal)) " +
           "FROM Transaction t " +
           "WHERE t.client.user.id = :userId " +
           "AND t.type = 'STP' " +
           "AND t.status = 'COMPLETED' " +
           "GROUP BY FUNCTION('DATE_FORMAT', t.transactionDate, '%Y-%m') " +
           "ORDER BY month DESC")
    List<StpTrendDTO> getMonthlyStpTrends(@Param("userId") Long userId);
    */

    // Additional helper methods for STP operations
    @Query("SELECT t FROM Transaction t " +
           "WHERE t.type = 'STP' " +
           "AND t.status = 'ACTIVE' " +
           "AND t.nextTransactionDate <= CURRENT_DATE")
    List<Transaction> findPendingStpTransactions();

    @Query("SELECT t FROM Transaction t " +
           "WHERE t.type = 'STP' " +
           "AND t.client.user.id = :userId " +
           "AND t.status = 'ACTIVE'")
    List<Transaction> findActiveStpsByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT DATE_FORMAT(t.transaction_date, '%Y-%m'), SUM(t.amount) FROM transactions t WHERE t.client_id = :userId AND t.type = 'STP' AND t.status = 'COMPLETED' GROUP BY DATE_FORMAT(t.transaction_date, '%Y-%m')", nativeQuery = true)
    List<Object[]> getMonthlyStpTrendsNative(@Param("userId") Long userId);
} 