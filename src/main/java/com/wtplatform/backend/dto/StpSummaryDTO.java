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
} 