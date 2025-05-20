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
    private int activeStps;
    private int executingToday;
    private int expiringNext3Months;
    private int zeroBalanceCount;
    private List<StpTrendDTO> monthlyStpTrends;
} 