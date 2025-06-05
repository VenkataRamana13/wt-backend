package com.wtplatform.backend.service;

import com.wtplatform.backend.model.Transaction;
import com.wtplatform.backend.dto.StpSummaryDTO;
import com.wtplatform.backend.dto.StpTransactionDTO;

import java.util.List;

public interface StpService {
    StpSummaryDTO getStpSummary(Long userId);
    StpSummaryDTO getStpSummaryByEmail(String email);
    List<StpTransactionDTO> getStpListByEmail(String email);
    void validateStpTransaction(Transaction transaction);
    void processStpTransaction(Transaction transaction);
    void debugStpTrendTypes(Long userId);
} 