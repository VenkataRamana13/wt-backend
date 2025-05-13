package com.wtplatform.backend.service;

import com.wtplatform.backend.dto.TransactionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    
    // Get all transactions (filtered by current user's access)
    List<TransactionDTO> getAllTransactions();
    
    // Get transaction by ID
    TransactionDTO getTransactionById(Long id);
    
    // Get transactions by client ID
    List<TransactionDTO> getTransactionsByClientId(Long clientId);
    
    // Get paginated transactions by client ID
    Page<TransactionDTO> getPagedTransactionsByClientId(Long clientId, Pageable pageable);
    
    // Get transactions by type
    List<TransactionDTO> getTransactionsByType(String type);
    
    // Get transactions by client ID and type
    List<TransactionDTO> getTransactionsByClientIdAndType(Long clientId, String type);
    
    // Get transactions by status
    List<TransactionDTO> getTransactionsByStatus(String status);
    
    // Get transactions by date range
    List<TransactionDTO> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate);
    
    // Get transactions by client ID and date range
    List<TransactionDTO> getTransactionsByClientIdAndDateRange(Long clientId, LocalDate startDate, LocalDate endDate);
    
    // Add a new transaction
    TransactionDTO addTransaction(TransactionDTO transactionDTO);
    
    // Update a transaction
    TransactionDTO updateTransaction(Long id, TransactionDTO transactionDTO);
    
    // Delete a transaction
    void deleteTransaction(Long id);
} 