package com.wtplatform.backend.service;

import com.wtplatform.backend.dto.DashboardStatsDTO;

public interface DashboardService {
    /**
     * Get dashboard statistics
     * 
     * @return dashboard statistics
     */
    DashboardStatsDTO getDashboardStats();
} 