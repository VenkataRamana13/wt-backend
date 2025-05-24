package com.wtplatform.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions_extended")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "client_name", length = 128)
    private String clientName;

    @Column(name = "transaction_id", length = 64)
    private String transactionId;

    @Column(name = "transaction_type", length = 32, nullable = false)
    private String type; // SIP, STP, SWP, LUMPSUM

    @Column(length = 32)
    private String mode;

    @Column(name = "fund_name", length = 128)
    private String fundName;

    @Column(name = "scheme_code", length = 32)
    private String schemeCode;

    @Column(name = "asset_class", length = 32)
    private String assetClass;

    @Column(name = "transaction_date")
    private LocalDate transactionDate;

    @Column(precision = 20, scale = 6, nullable = false)
    private BigDecimal amount;

    @Column(precision = 20, scale = 6)
    private BigDecimal units;

    @Column(name = "nav_at_txn_time", precision = 20, scale = 6)
    private BigDecimal navAtTransactionTime;

    @Column(name = "is_recurring")
    private Boolean isRecurring;

    @Column(length = 32)
    private String frequency;

    @Column(name = "installment_number")
    private Integer installmentNumber;

    @Column(name = "total_installments")
    private Integer totalInstallments;

    @Column(name = "next_transaction_date")
    private LocalDate nextTransactionDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "from_fund", length = 128)
    private String fromFund;

    @Column(name = "to_fund", length = 128)
    private String toFund;

    @Column(length = 32)
    private String status;

    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods for STP operations
    public BigDecimal getSourceBalance() {
        // This should be implemented to get the balance from the source fund
        // You might want to add a source_balance column or calculate it dynamically
        return null; // TODO: Implement this
    }

    public void setSourceBalance(BigDecimal balance) {
        // This should be implemented to update the balance in the source fund
        // You might want to add a source_balance column or handle it differently
        // TODO: Implement this
    }

    public LocalDate getExpiryDate() {
        return this.endDate;
    }

    public void setNextExecutionDate(LocalDate date) {
        this.nextTransactionDate = date;
    }

    public String getFrequency() {
        return this.frequency;
    }

    public LocalDate getNextExecutionDate() {
        return this.nextTransactionDate;
    }
} 