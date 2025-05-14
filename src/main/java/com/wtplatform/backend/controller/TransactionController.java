package com.wtplatform.backend.controller;

import com.wtplatform.backend.dto.TransactionDTO;
import com.wtplatform.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        log.debug("Getting all transactions");
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        log.debug("Getting transaction with ID: {}", id);
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByClientId(@PathVariable Long clientId) {
        log.debug("Getting transactions for client ID: {}", clientId);
        return ResponseEntity.ok(transactionService.getTransactionsByClientId(clientId));
    }

    @GetMapping("/client/{clientId}/paged")
    public ResponseEntity<Page<TransactionDTO>> getPagedTransactionsByClientId(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        return ResponseEntity.ok(transactionService.getPagedTransactionsByClientId(clientId, pageable));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByType(@PathVariable String type) {
        return ResponseEntity.ok(transactionService.getTransactionsByType(type));
    }

    @GetMapping("/client/{clientId}/type/{type}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByClientIdAndType(
            @PathVariable Long clientId,
            @PathVariable String type
    ) {
        return ResponseEntity.ok(transactionService.getTransactionsByClientIdAndType(clientId, type));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(transactionService.getTransactionsByStatus(status));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(transactionService.getTransactionsByDateRange(startDate, endDate));
    }

    @GetMapping("/client/{clientId}/date-range")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByClientIdAndDateRange(
            @PathVariable Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(transactionService.getTransactionsByClientIdAndDateRange(clientId, startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> addTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Transaction creation request received - Type: {}, Amount: {}, ClientId: {}, User: {}", 
            transactionDTO.getType(), 
            transactionDTO.getAmount(),
            transactionDTO.getClientId(),
            auth != null ? auth.getName() : "unknown");
        
        try {
            TransactionDTO result = transactionService.addTransaction(transactionDTO);
            log.info("Transaction successfully created with ID: {}", result.getId());
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Transaction creation failed - Type: {}, ClientId: {}, Error: {}", 
                transactionDTO.getType(), 
                transactionDTO.getClientId(),
                e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDTO transactionDTO
    ) {
        log.debug("Updating transaction with ID: {}", id);
        return ResponseEntity.ok(transactionService.updateTransaction(id, transactionDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        log.debug("Deleting transaction with ID: {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
} 