package com.wtplatform.backend.dto;

import com.wtplatform.backend.model.Transaction;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    
    @NotNull(message = "Client ID is required")
    private Long clientId;
    
    private String clientName;
    
    @NotBlank(message = "Transaction type is required")
    private String type;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Date is required")
    private LocalDate transactionDate;
    
    @NotBlank(message = "Status is required")
    private String status;
    
    private String remarks;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Convert a Transaction entity to a DTO
     * 
     * @param transaction the entity to convert
     * @return the converted DTO
     */
    public static TransactionDTO fromEntity(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        return TransactionDTO.builder()
                .id(transaction.getId())
                .clientId(transaction.getClient().getId())
                .clientName(transaction.getClient().getName())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .transactionDate(transaction.getTransactionDate())
                .status(transaction.getStatus())
                .remarks(transaction.getRemarks())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert a list of Transaction entities to DTOs
     * 
     * @param transactions the list of entities to convert
     * @return the list of converted DTOs
     */
    public static List<TransactionDTO> fromEntities(List<Transaction> transactions) {
        if (transactions == null) {
            return List.of();
        }
        
        return transactions.stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
    }
} 