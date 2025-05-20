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
@Table(name = "transactions")
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

    @Column(nullable = false)
    private String type; // SIP, STP, SWP, LUMPSUM

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String status; // completed, pending, failed, active

    @Column(columnDefinition = "TEXT")
    private String description;
    
    // Fields for STP/SIP functionality
    @Column
    private String frequency; // Monthly, Quarterly, etc.
    
    @Column
    private LocalDate nextExecutionDate;
    
    @Column
    private LocalDate expiryDate;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal remainingAmount;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal sourceBalance;
    
    @Column
    private String fromScheme; // For STP: source fund/scheme
    
    @Column
    private String toScheme; // For STP: target fund/scheme

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
} 