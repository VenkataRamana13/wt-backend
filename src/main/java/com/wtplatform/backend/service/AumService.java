package com.wtplatform.backend.service;

import com.wtplatform.backend.dto.AumSummaryDTO;
import com.wtplatform.backend.dto.AumTrendDTO;
import com.wtplatform.backend.dto.AumBreakdownDTO;
import java.util.List;

public interface AumService {
    /**
     * Get AUM summary including total AUM, contributing clients count, and last updated timestamp
     */
    AumSummaryDTO getAumSummary();

    /**
     * Get AUM trend data for the specified period
     * @param period The time period (1m, 3m, 6m, 1y, etc.)
     */
    List<AumTrendDTO> getAumTrend(String period);

    /**
     * Get detailed AUM breakdown by client, asset class, and time segments
     */
    AumBreakdownDTO getAumBreakdown();
} 