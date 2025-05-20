package com.wtplatform.backend.service.impl;

import com.wtplatform.backend.dto.StpSummaryDTO;
import com.wtplatform.backend.dto.StpTrendDTO;
import com.wtplatform.backend.model.Transaction;
import com.wtplatform.backend.model.User;
import com.wtplatform.backend.repository.TransactionRepository;
import com.wtplatform.backend.service.StpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StpServiceImpl implements StpService {

    private static final Logger log = LoggerFactory.getLogger(StpServiceImpl.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public StpSummaryDTO getStpSummary(int monthsBack) {
        log.info("Getting STP summary with {} months of trend data", monthsBack);
        
        try {
            LocalDate now = LocalDate.now();
            LocalDate start = now.minusMonths(monthsBack - 1).withDayOfMonth(1);
            
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = getUserIdFromAuth(auth);
            
            // Fetch all transactions for this user
            List<Transaction> allUserTransactions = transactionRepository.findByUserId(userId);
            log.info("Found {} transactions in total for user ID: {}", allUserTransactions.size(), userId);
            
            // Log all unique transaction types to help with debugging
            Set<String> uniqueTypes = allUserTransactions.stream()
                    .map(Transaction::getType)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            log.info("Unique transaction types in database: {}", uniqueTypes);
            
            // More robust filtering for STP transactions
            List<Transaction> stpTransactions = allUserTransactions.stream()
                    .filter(tx -> tx.getType() != null && tx.getType().trim().equalsIgnoreCase("STP"))
                    .collect(Collectors.toList());
            
            log.info("Found {} STP transactions for user ID: {}", stpTransactions.size(), userId);
            
            // Log STP transaction details for debugging
            if (!stpTransactions.isEmpty()) {
                Transaction firstStp = stpTransactions.get(0);
                log.info("Sample STP transaction - ID: {}, Type: '{}', Amount: {}, Status: '{}', Date: {}", 
                         firstStp.getId(), firstStp.getType(), firstStp.getAmount(), 
                         firstStp.getStatus(), firstStp.getDate());
            } else {
                log.warn("No STP transactions found. Returning zero counts.");
            }
            
            // Count metrics for summary cards
            int activeStps = (int) stpTransactions.stream()
                    .filter(tx -> "Active".equalsIgnoreCase(tx.getStatus()))
                    .count();
            log.info("Active STPs count: {}", activeStps);
            
            LocalDate today = LocalDate.now();
            int executingToday = (int) stpTransactions.stream()
                    .filter(tx -> tx.getNextExecutionDate() != null && tx.getNextExecutionDate().isEqual(today))
                    .count();
            log.info("Executing today count: {}", executingToday);
            
            LocalDate threeMonthsFromNow = today.plusMonths(3);
            int expiringNext3Months = (int) stpTransactions.stream()
                    .filter(tx -> tx.getExpiryDate() != null && 
                           tx.getExpiryDate().isAfter(today) && 
                           tx.getExpiryDate().isBefore(threeMonthsFromNow))
                    .count();
            log.info("Expiring in next 3 months count: {}", expiringNext3Months);
            
            int zeroBalanceCount = (int) stpTransactions.stream()
                    .filter(tx -> tx.getSourceBalance() != null && tx.getSourceBalance().doubleValue() == 0)
                    .count();
            log.info("Zero source balance count: {}", zeroBalanceCount);
            
            // Calculate monthly trends
            Map<String, Double> monthToAmount = new LinkedHashMap<>();
            
            // Initialize with all months in range (with zero values)
            for (int i = 0; i < monthsBack; i++) {
                LocalDate month = now.minusMonths(monthsBack - 1 - i);
                String monthName = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                monthToAmount.put(monthName, 0.0);
            }
            
            // Aggregate amounts by month
            for (Transaction tx : stpTransactions) {
                if (tx.getDate() != null && !tx.getDate().isBefore(start)) {
                    String monthName = tx.getDate().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                    
                    // Only update if the month is in our map (within our range)
                    monthToAmount.computeIfPresent(monthName, (k, v) -> 
                        v + (tx.getAmount() != null ? tx.getAmount().doubleValue() : 0.0)
                    );
                }
            }
            
            // Log monthly trends
            log.info("Monthly STP trends: {}", monthToAmount);
            
            // Convert map to list of DTOs
            List<StpTrendDTO> monthlyTrends = monthToAmount.entrySet().stream()
                    .map(entry -> new StpTrendDTO(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            
            // Build and return the summary
            StpSummaryDTO summary = StpSummaryDTO.builder()
                    .activeStps(activeStps)
                    .executingToday(executingToday)
                    .expiringNext3Months(expiringNext3Months)
                    .zeroBalanceCount(zeroBalanceCount)
                    .monthlyStpTrends(monthlyTrends)
                    .build();
            
            log.info("Returning STP summary: {}", summary);
            return summary;
                    
        } catch (Exception e) {
            log.error("Error getting STP summary", e);
            throw e;
        }
    }
    
    private Long getUserIdFromAuth(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            log.error("Authentication or principal is null");
            throw new IllegalStateException("User not authenticated");
        }
        
        if (auth.getPrincipal() instanceof User) {
            return ((User) auth.getPrincipal()).getId();
        } else {
            log.error("Principal is not of type User: {}", auth.getPrincipal().getClass().getName());
            throw new IllegalStateException("User ID not found in authentication");
        }
    }
} 