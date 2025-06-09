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

    public NavHistory(String schemeCode, LocalDate navDate, BigDecimal navValue, String source) {
        this.fundId = Long.parseLong(schemeCode);
        this.navDate = navDate;
        this.nav = navValue;
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFundId() {
        return fundId;
    }

    public void setFundId(Long fundId) {
        this.fundId = fundId;
    }

    public LocalDate getNavDate() {
        return navDate;
    }

    public void setNavDate(LocalDate navDate) {
        this.navDate = navDate;
    }

    public BigDecimal getNav() {
        return nav;
    }

    public void setNav(BigDecimal nav) {
        this.nav = nav;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
} 