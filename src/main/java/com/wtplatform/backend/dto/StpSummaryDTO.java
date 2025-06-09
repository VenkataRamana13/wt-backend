package com.wtplatform.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StpSummaryDTO {
    private Long activeStps;
    private Long executingToday;
    private Long expiringNext3Months;
    private Long zeroBalanceCount;
    private List<StpTrendDTO> monthlyTrends;

    public Long getActiveStps() {
        return activeStps;
    }

    public void setActiveStps(Long activeStps) {
        this.activeStps = activeStps;
    }

    public Long getExecutingToday() {
        return executingToday;
    }

    public void setExecutingToday(Long executingToday) {
        this.executingToday = executingToday;
    }

    public Long getExpiringNext3Months() {
        return expiringNext3Months;
    }

    public void setExpiringNext3Months(Long expiringNext3Months) {
        this.expiringNext3Months = expiringNext3Months;
    }

    public Long getZeroBalanceCount() {
        return zeroBalanceCount;
    }

    public void setZeroBalanceCount(Long zeroBalanceCount) {
        this.zeroBalanceCount = zeroBalanceCount;
    }

    public List<StpTrendDTO> getMonthlyTrends() {
        return monthlyTrends;
    }

    public void setMonthlyTrends(List<StpTrendDTO> monthlyTrends) {
        this.monthlyTrends = monthlyTrends;
    }
} 