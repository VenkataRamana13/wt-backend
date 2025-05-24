package com.wtplatform.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "nav_history", uniqueConstraints = @UniqueConstraint(columnNames = {"fund_id", "nav_date"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fund_id", nullable = false)
    private Long fundId;

    @Column(name = "nav_date", nullable = false)
    private LocalDate navDate;

    @Column(precision = 20, scale = 6, nullable = false)
    private BigDecimal nav;

    @Column(length = 64, nullable = false)
    private String source;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now();
} 