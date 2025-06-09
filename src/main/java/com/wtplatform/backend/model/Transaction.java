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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public String getSchemeCode() {
        return schemeCode;
    }

    public void setSchemeCode(String schemeCode) {
        this.schemeCode = schemeCode;
    }

    public String getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(String assetClass) {
        this.assetClass = assetClass;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getUnits() {
        return units;
    }

    public void setUnits(BigDecimal units) {
        this.units = units;
    }

    public BigDecimal getNavAtTransactionTime() {
        return navAtTransactionTime;
    }

    public void setNavAtTransactionTime(BigDecimal navAtTransactionTime) {
        this.navAtTransactionTime = navAtTransactionTime;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(Integer installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public Integer getTotalInstallments() {
        return totalInstallments;
    }

    public void setTotalInstallments(Integer totalInstallments) {
        this.totalInstallments = totalInstallments;
    }

    public LocalDate getNextTransactionDate() {
        return nextTransactionDate;
    }

    public void setNextTransactionDate(LocalDate nextTransactionDate) {
        this.nextTransactionDate = nextTransactionDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getFromFund() {
        return fromFund;
    }

    public void setFromFund(String fromFund) {
        this.fromFund = fromFund;
    }

    public String getToFund() {
        return toFund;
    }

    public void setToFund(String toFund) {
        this.toFund = toFund;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
} 