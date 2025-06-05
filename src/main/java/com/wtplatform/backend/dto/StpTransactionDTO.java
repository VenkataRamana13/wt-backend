package com.wtplatform.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StpTransactionDTO {
    private Long id;
    private String clientId;
    private String clientName;
    private BigDecimal amount;
    private String fromFund;
    private String toFund;
    private String frequency;
    private LocalDate nextTransactionDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal remainingAmount;
    private BigDecimal sourceBalance;
    private String status;
    private String remarks;
} 