package com.wtplatform.backend.service.impl;

import com.wtplatform.backend.dto.TransactionDTO;
import com.wtplatform.backend.model.Client;
import com.wtplatform.backend.model.Transaction;
import com.wtplatform.backend.model.User;
import com.wtplatform.backend.repository.ClientRepository;
import com.wtplatform.backend.repository.TransactionRepository;
import com.wtplatform.backend.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getAllTransactions() {
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        log.info("Getting all transactions for user: {}", username);
        log.debug("Authentication principal: {}, type: {}", 
            auth.getPrincipal(), 
            auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null");
        
        try {
            // Only return transactions for clients belonging to the current user
            Long userId = getUserIdFromAuth(auth);
            log.info("Extracted user ID: {}", userId);
            
            List<Transaction> transactions = transactionRepository.findByUserId(userId);
            log.info("Found {} transactions for user ID: {}", transactions.size(), userId);
            
            return TransactionDTO.fromEntities(transactions);
        } catch (Exception e) {
            log.error("Error getting all transactions", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(Long id) {
        log.info("Getting transaction with ID: {}", id);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));
        
        // Check if the transaction belongs to a client of the current user
        validateUserAccess(transaction);
        
        return TransactionDTO.fromEntity(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByClientId(Long clientId) {
        log.info("Getting transactions for client ID: {}", clientId);
        
        // Check if the client belongs to the current user
        validateClientAccess(clientId);
        
        List<Transaction> transactions = transactionRepository.findByClientId(clientId);
        log.info("Found {} transactions for client ID: {}", transactions.size(), clientId);
        
        return TransactionDTO.fromEntities(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getPagedTransactionsByClientId(Long clientId, Pageable pageable) {
        log.info("Getting paged transactions for client ID: {}, page: {}, size: {}", 
            clientId, pageable.getPageNumber(), pageable.getPageSize());
            
        // Check if the client belongs to the current user
        validateClientAccess(clientId);
        
        Page<Transaction> transactionPage = transactionRepository.findByClientId(clientId, pageable);
        
        List<TransactionDTO> transactionDTOs = transactionPage.getContent().stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
        
        log.info("Found {} transactions (page {} of {}) for client ID: {}", 
            transactionPage.getNumberOfElements(), 
            transactionPage.getNumber() + 1, 
            transactionPage.getTotalPages(),
            clientId);
            
        return new PageImpl<>(transactionDTOs, pageable, transactionPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByType(String type) {
        log.info("Getting transactions with type: {}", type);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        log.debug("Authentication principal for getTransactionsByType: {}, type: {}", 
            auth.getPrincipal(), 
            auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null");
        
        try {
            // First get transactions by type
            List<Transaction> allTransactionsByType = transactionRepository.findByType(type);
            log.info("Found {} transactions with type: {}", allTransactionsByType.size(), type);
            
            // Then filter to only include those for clients belonging to the current user
            Long userId = getUserIdFromAuth(auth);
            List<Transaction> filteredTransactions = allTransactionsByType.stream()
                    .filter(t -> t.getClient().getUser().getId().equals(userId))
                    .collect(Collectors.toList());
            
            log.info("After filtering for user {}, found {} transactions with type: {}", 
                username, filteredTransactions.size(), type);
                
            return TransactionDTO.fromEntities(filteredTransactions);
        } catch (Exception e) {
            log.error("Error getting transactions by type: {}", type, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByClientIdAndType(Long clientId, String type) {
        log.info("Getting transactions for client ID: {} with type: {}", clientId, type);
        
        // Check if the client belongs to the current user
        validateClientAccess(clientId);
        
        List<Transaction> transactions = transactionRepository.findByClientIdAndType(clientId, type);
        log.info("Found {} transactions for client ID: {} with type: {}", 
            transactions.size(), clientId, type);
            
        return TransactionDTO.fromEntities(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByStatus(String status) {
        log.info("Getting transactions with status: {}", status);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            Long userId = getUserIdFromAuth(auth);
            
            // First get transactions by status
            List<Transaction> allTransactionsByStatus = transactionRepository.findByStatus(status);
            log.info("Found {} transactions with status: {}", allTransactionsByStatus.size(), status);
            
            // Then filter to only include those for clients belonging to the current user
            List<Transaction> filteredTransactions = allTransactionsByStatus.stream()
                    .filter(t -> t.getClient().getUser().getId().equals(userId))
                    .collect(Collectors.toList());
                    
            log.info("After filtering for user ID {}, found {} transactions with status: {}", 
                userId, filteredTransactions.size(), status);
                
            return TransactionDTO.fromEntities(filteredTransactions);
        } catch (Exception e) {
            log.error("Error getting transactions by status: {}", status, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Getting transactions between {} and {}", startDate, endDate);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            Long userId = getUserIdFromAuth(auth);
            
            // First get transactions by date range
            List<Transaction> allTransactionsByDateRange = transactionRepository.findByDateBetween(startDate, endDate);
            log.info("Found {} transactions between {} and {}", 
                allTransactionsByDateRange.size(), startDate, endDate);
                
            // Then filter to only include those for clients belonging to the current user
            List<Transaction> filteredTransactions = allTransactionsByDateRange.stream()
                    .filter(t -> t.getClient().getUser().getId().equals(userId))
                    .collect(Collectors.toList());
                    
            log.info("After filtering for user ID {}, found {} transactions between {} and {}", 
                userId, filteredTransactions.size(), startDate, endDate);
                
            return TransactionDTO.fromEntities(filteredTransactions);
        } catch (Exception e) {
            log.error("Error getting transactions by date range: {} to {}", startDate, endDate, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByClientIdAndDateRange(Long clientId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting transactions for client ID: {} between {} and {}", clientId, startDate, endDate);
        
        // Check if the client belongs to the current user
        validateClientAccess(clientId);
        
        List<Transaction> transactions = transactionRepository.findByClientIdAndDateBetween(clientId, startDate, endDate);
        log.info("Found {} transactions for client ID: {} between {} and {}", 
            transactions.size(), clientId, startDate, endDate);
            
        return TransactionDTO.fromEntities(transactions);
    }

    @Override
    @Transactional
    public TransactionDTO addTransaction(TransactionDTO transactionDTO) {
        log.info("Adding new transaction for client ID: {} of type: {}", 
            transactionDTO.getClientId(), transactionDTO.getType());
            
        try {
            // Check if the client exists and belongs to the current user
            Long clientId = transactionDTO.getClientId();
            Client client = validateClientAccess(clientId);
            
            // Create new transaction entity
            Transaction transaction = Transaction.builder()
                    .client(client)
                    .type(transactionDTO.getType())
                    .amount(transactionDTO.getAmount())
                    .date(transactionDTO.getDate())
                    .status(transactionDTO.getStatus())
                    .description(transactionDTO.getDescription())
                    .build();
            
            // Save the transaction
            Transaction savedTransaction = transactionRepository.save(transaction);
            log.info("Successfully saved transaction with ID: {} for client ID: {}", 
                savedTransaction.getId(), client.getId());
                
            return TransactionDTO.fromEntity(savedTransaction);
        } catch (Exception e) {
            log.error("Error adding transaction for client ID: {}", transactionDTO.getClientId(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public TransactionDTO updateTransaction(Long id, TransactionDTO transactionDTO) {
        log.info("Updating transaction with ID: {}", id);
        
        // Check if the transaction exists
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));
        
        // Check if the user has access to this transaction
        validateUserAccess(existingTransaction);
        
        // Check if the client exists and belongs to the current user
        Long clientId = transactionDTO.getClientId();
        Client client = validateClientAccess(clientId);
        
        // Update transaction fields
        existingTransaction.setClient(client);
        existingTransaction.setType(transactionDTO.getType());
        existingTransaction.setAmount(transactionDTO.getAmount());
        existingTransaction.setDate(transactionDTO.getDate());
        existingTransaction.setStatus(transactionDTO.getStatus());
        existingTransaction.setDescription(transactionDTO.getDescription());
        
        // Save the updated transaction
        Transaction updatedTransaction = transactionRepository.save(existingTransaction);
        log.info("Successfully updated transaction with ID: {}", id);
        
        return TransactionDTO.fromEntity(updatedTransaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {
        log.info("Deleting transaction with ID: {}", id);
        
        // Check if the transaction exists
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));
        
        // Check if the user has access to this transaction
        validateUserAccess(transaction);
        
        // Delete the transaction
        transactionRepository.delete(transaction);
        log.info("Successfully deleted transaction with ID: {}", id);
    }
    
    // Helper method to get user ID from authentication
    private Long getUserIdFromAuth(Authentication auth) {
        log.debug("Getting user ID from Authentication");
        log.debug("Authentication principal: {}, type: {}", 
            auth.getPrincipal(), 
            auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null");
        
        if (auth.getPrincipal() == null) {
            log.error("Authentication principal is null");
            throw new SecurityException("Authentication principal is null");
        }
        
        try {
            if (auth.getPrincipal() instanceof User) {
                User user = (User) auth.getPrincipal();
                log.debug("Principal is User entity with ID: {}", user.getId());
                return user.getId();
            } else {
                log.error("Authentication principal is not a User entity: {}", auth.getPrincipal().getClass().getName());
                throw new SecurityException("Authentication principal is not a User entity");
            }
        } catch (ClassCastException e) {
            log.error("Failed to cast Authentication principal to User", e);
            throw new SecurityException("Failed to cast Authentication principal to User", e);
        }
    }
    
    // Helper method to validate user access to a client
    private Client validateClientAccess(Long clientId) {
        log.debug("Validating access to client with ID: {}", clientId);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Authentication: {}", auth);
        log.debug("Authentication principal: {}, type: {}", 
            auth.getPrincipal(), 
            auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null");
        
        try {
            User currentUser = (User) auth.getPrincipal();
            log.debug("Current user ID: {}", currentUser.getId());
            
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + clientId));
            log.debug("Client found with ID: {}, belongs to user ID: {}", 
                client.getId(), client.getUser().getId());
            
            if (!client.getUser().getId().equals(currentUser.getId())) {
                log.warn("Access denied: User ID {} tried to access client ID {} belonging to user ID {}",
                    currentUser.getId(), clientId, client.getUser().getId());
                throw new SecurityException("You do not have access to this client");
            }
            
            log.debug("Access validated for client ID: {}", clientId);
            return client;
        } catch (ClassCastException e) {
            log.error("Failed to cast Authentication principal to User", e);
            throw new SecurityException("Failed to cast Authentication principal to User", e);
        }
    }
    
    // Helper method to validate user access to a transaction
    private void validateUserAccess(Transaction transaction) {
        log.debug("Validating access to transaction with ID: {}", transaction.getId());
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Authentication principal: {}, type: {}", 
            auth.getPrincipal(), 
            auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null");
        
        try {
            User currentUser = (User) auth.getPrincipal();
            log.debug("Current user ID: {}", currentUser.getId());
            log.debug("Transaction client user ID: {}", transaction.getClient().getUser().getId());
            
            if (!transaction.getClient().getUser().getId().equals(currentUser.getId())) {
                log.warn("Access denied: User ID {} tried to access transaction ID {} belonging to user ID {}",
                    currentUser.getId(), transaction.getId(), transaction.getClient().getUser().getId());
                throw new SecurityException("You do not have access to this transaction");
            }
            
            log.debug("Access validated for transaction ID: {}", transaction.getId());
        } catch (ClassCastException e) {
            log.error("Failed to cast Authentication principal to User", e);
            throw new SecurityException("Failed to cast Authentication principal to User", e);
        }
    }
} 