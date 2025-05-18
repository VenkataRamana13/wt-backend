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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        log.info("Transaction creation initiated - Type: {}, Amount: {}, ClientId: {}, Date: {}", 
            transactionDTO.getType(), 
            transactionDTO.getAmount(),
            transactionDTO.getClientId(),
            transactionDTO.getDate());
            
        try {
            // Check if the client exists and belongs to the current user
            Long clientId = transactionDTO.getClientId();
            log.debug("Validating client access for clientId: {}", clientId);
            
            Client client;
            try {
                client = validateClientAccess(clientId);
                log.debug("Client validation successful. Client name: {}", client.getName());
            } catch (EntityNotFoundException e) {
                log.error("Client not found during transaction creation - ClientId: {}", clientId);
                throw e;
            } catch (SecurityException e) {
                log.error("Security violation during transaction creation - ClientId: {} is not accessible to the current user", clientId);
                throw e;
            }
            
            // Create new transaction entity
            log.debug("Creating transaction entity");
            Transaction transaction = Transaction.builder()
                    .client(client)
                    .type(transactionDTO.getType())
                    .amount(transactionDTO.getAmount())
                    .date(transactionDTO.getDate())
                    .status(transactionDTO.getStatus())
                    .description(transactionDTO.getDescription())
                    .build();
            
            // Save the transaction
            log.debug("Saving transaction to the database");
            Transaction savedTransaction = transactionRepository.save(transaction);
            log.info("Transaction created successfully - ID: {}, Type: {}, Amount: {}, ClientId: {}, ClientName: {}", 
                savedTransaction.getId(), 
                savedTransaction.getType(), 
                savedTransaction.getAmount(),
                client.getId(),
                client.getName());
                
            return TransactionDTO.fromEntity(savedTransaction);
        } catch (Exception e) {
            if (!(e instanceof EntityNotFoundException || e instanceof SecurityException)) {
                log.error("Unexpected error during transaction creation - Type: {}, ClientId: {}, Error: {}", 
                    transactionDTO.getType(), 
                    transactionDTO.getClientId(),
                    e.getMessage(), e);
            }
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

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getRecentTransactions(int limit) {
        log.info("Getting {} most recent transactions", limit);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            Long userId = getUserIdFromAuth(auth);
            
            // Use Spring's Pageable to limit results
            Pageable pageable = PageRequest.of(0, limit);
            
            // Get transactions in reverse chronological order
            List<Transaction> recentTransactions = transactionRepository.findByUserIdOrderByDateDesc(userId, pageable);
            log.info("Found {} recent transactions for user ID: {}", recentTransactions.size(), userId);
            
            return TransactionDTO.fromEntities(recentTransactions);
        } catch (Exception e) {
            log.error("Error getting recent transactions", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public List<TransactionDTO> importFromCSV(MultipartFile file) {
        log.info("Starting CSV import for transactions");
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            Long userId = getUserIdFromAuth(SecurityContextHolder.getContext().getAuthentication());
            
            // Create a map of clientId to Client entity for quick access
            List<Client> userClients = clientRepository.findByUserId(userId);
            Map<Long, Client> clientMap = new HashMap<>();
            for (Client client : userClients) {
                clientMap.put(client.getId(), client);
            }
            
            List<Transaction> importedTransactions = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            
            for (CSVRecord record : csvParser) {
                try {
                    // Extract required fields
                    String clientIdStr = record.get("clientId");
                    String clientName = record.get("clientName"); // Used for validation
                    String type = record.get("type");
                    String amountStr = record.get("amount");
                    String dateStr = record.get("date");
                    String status = record.get("status");
                    
                    // Get description if it exists
                    String description = record.isMapped("description") ? record.get("description") : null;
                    
                    // Validate and convert types
                    Long clientId;
                    try {
                        clientId = Long.parseLong(clientIdStr);
                    } catch (NumberFormatException e) {
                        errors.add("Row " + record.getRecordNumber() + ": Invalid client ID: " + clientIdStr);
                        continue;
                    }
                    
                    // Ensure client exists and belongs to the current user
                    Client client = clientMap.get(clientId);
                    if (client == null) {
                        errors.add("Row " + record.getRecordNumber() + ": Client with ID " + clientId + " not found or access denied");
                        continue;
                    }
                    
                    // Validate transaction type
                    if (!isValidTransactionType(type)) {
                        errors.add("Row " + record.getRecordNumber() + ": Invalid transaction type: " + type);
                        continue;
                    }
                    
                    // Parse amount
                    BigDecimal amount;
                    try {
                        amount = new BigDecimal(amountStr);
                    } catch (NumberFormatException e) {
                        errors.add("Row " + record.getRecordNumber() + ": Invalid amount: " + amountStr);
                        continue;
                    }
                    
                    // Parse date
                    LocalDate date;
                    try {
                        date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
                    } catch (DateTimeParseException e) {
                        errors.add("Row " + record.getRecordNumber() + ": Invalid date format: " + dateStr + ". Use yyyy-MM-dd format.");
                        continue;
                    }
                    
                    // Validate status
                    if (!isValidStatus(status)) {
                        errors.add("Row " + record.getRecordNumber() + ": Invalid status: " + status);
                        continue;
                    }
                    
                    // Create and add transaction
                    Transaction transaction = new Transaction();
                    transaction.setClient(client);
                    transaction.setType(type);
                    transaction.setAmount(amount);
                    transaction.setDate(date);
                    transaction.setStatus(status);
                    transaction.setDescription(description);
                    
                    importedTransactions.add(transaction);
                    
                } catch (Exception e) {
                    errors.add("Row " + record.getRecordNumber() + ": Error processing record: " + e.getMessage());
                }
            }
            
            // If there are errors, log them and throw an exception
            if (!errors.isEmpty()) {
                String errorMessage = String.join("\n", errors);
                log.error("CSV import errors: {}", errorMessage);
                throw new IllegalArgumentException("CSV import failed with errors:\n" + errorMessage);
            }
            
            // Save all transactions
            List<Transaction> savedTransactions = transactionRepository.saveAll(importedTransactions);
            log.info("Successfully imported {} transactions", savedTransactions.size());
            
            return TransactionDTO.fromEntities(savedTransactions);
            
        } catch (IOException e) {
            log.error("Error reading CSV file: {}", e.getMessage(), e);
            throw new RuntimeException("Error reading CSV file: " + e.getMessage(), e);
        }
    }
    
    private boolean isValidTransactionType(String type) {
        return type != null && (
            "SIP".equalsIgnoreCase(type) || 
            "STP".equalsIgnoreCase(type) || 
            "SWP".equalsIgnoreCase(type) || 
            "LUMPSUM".equalsIgnoreCase(type)
        );
    }
    
    private boolean isValidStatus(String status) {
        return status != null && (
            "completed".equalsIgnoreCase(status) || 
            "pending".equalsIgnoreCase(status) || 
            "failed".equalsIgnoreCase(status)
        );
    }
} 