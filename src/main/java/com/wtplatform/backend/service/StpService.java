package com.wtplatform.backend.service;

import com.wtplatform.backend.dto.StpSummaryDTO;

public interface StpService {
    /**
     * Get STP summary data including active counts, trends, etc.
     *
     * @param monthsBack Number of months to look back for trend data
     * @return StpSummaryDTO with all STP statistics
     */
    StpSummaryDTO getStpSummary(int monthsBack);
} 