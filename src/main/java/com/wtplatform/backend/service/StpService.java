package com.wtplatform.backend.service;

import com.wtplatform.backend.dto.StpSummaryDTO;

public interface StpService {
    StpSummaryDTO getStpSummary(Long userId);
    void validateStpTransaction(Transaction transaction);
    void processStpTransaction(Transaction transaction);
} 