package com.wtplatform.backend.service;

import com.wtplatform.backend.model.Transaction;
import com.wtplatform.backend.dto.StpSummaryDTO;

public interface StpService {
    StpSummaryDTO getStpSummary(Long userId);
    StpSummaryDTO getStpSummaryByEmail(String email);
    void validateStpTransaction(Transaction transaction);
    void processStpTransaction(Transaction transaction);
    void debugStpTrendTypes(Long userId);
} 