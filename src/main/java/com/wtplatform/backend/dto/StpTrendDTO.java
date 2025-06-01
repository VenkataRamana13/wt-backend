package com.wtplatform.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@NoArgsConstructor
public class StpTrendDTO {
    private String month;
    private BigDecimal amount;
    private BigInteger count;

    // Constructor specifically for JPA projection query
    public StpTrendDTO(String month, BigDecimal amount, BigInteger count) {
        this.month = month.toString();
        this.amount = amount;
        this.count = count;
    }
} 