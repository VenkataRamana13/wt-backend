package com.wtplatform.backend.repository;

import com.wtplatform.backend.model.Transaction;
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
    List<Transaction> findByClientIdAndDateBetween(Long clientId, LocalDate startDate, LocalDate endDate);
    
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
    @Query("SELECT t FROM Transaction t JOIN t.client c WHERE c.user.id = :userId ORDER BY t.date DESC")
    List<Transaction> findByUserIdOrderByDateDesc(@Param("userId") Long userId);
    
    /**
     * Find limited number of transactions for clients belonging to a specific user ordered by date (descending)
     */
    @Query("SELECT t FROM Transaction t JOIN t.client c WHERE c.user.id = :userId ORDER BY t.date DESC")
    List<Transaction> findByUserIdOrderByDateDesc(@Param("userId") Long userId, Pageable pageable);
    
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
    List<Transaction> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
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
} 