package com.wtplatform.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fund_balance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fund_id", length = 128, nullable = false)
    private String fundId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(precision = 20, scale = 6, nullable = false)
    private BigDecimal balance;

    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now();
} 