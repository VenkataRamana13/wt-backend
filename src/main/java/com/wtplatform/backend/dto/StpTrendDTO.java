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
    private String fromFund;
    private String toFund;

    // Constructor specifically for JPA projection query
    public StpTrendDTO(String month, BigDecimal amount, BigInteger count, String fromFund, String toFund) {
        this.month = month.toString();
        this.amount = amount;
        this.count = count;
        this.fromFund = fromFund;
        this.toFund = toFund;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigInteger getCount() {
        return count;
    }

    public void setCount(BigInteger count) {
        this.count = count;
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
} 