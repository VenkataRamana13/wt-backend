package com.wtplatform.backend.service.impl;

import com.wtplatform.backend.dto.StpSummaryDTO;
import com.wtplatform.backend.dto.StpTrendDTO;
import com.wtplatform.backend.exception.InsufficientBalanceException;
import com.wtplatform.backend.exception.InvalidTransactionException;
import com.wtplatform.backend.model.Transaction;
import com.wtplatform.backend.model.FundBalance;
import com.wtplatform.backend.repository.TransactionRepository;
import com.wtplatform.backend.repository.FundBalanceRepository;
import com.wtplatform.backend.service.StpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StpServiceImpl implements StpService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FundBalanceRepository fundBalanceRepository;

    @Override
    public StpSummaryDTO getStpSummary(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate threemonthsLater = today.plusMonths(3);

        Long activeStps = transactionRepository.countActiveStpsByUserId(userId);
        Long executingToday = transactionRepository.countStpsExecutingToday(userId, today);
        Long expiringNext3Months = transactionRepository.countStpsExpiringBetween(userId, today, threemonthsLater);
        Long zeroBalanceCount = transactionRepository.countStpsWithZeroBalance(userId);
        
        // Use native query and map results to DTOs
        List<Object[]> trendResults = transactionRepository.getMonthlyStpTrendsNative(userId);
        List<StpTrendDTO> monthlyTrends = trendResults.stream()
            .map(row -> new StpTrendDTO(
                (String) row[0],  // month
                (BigDecimal) row[1]  // amount
            ))
            .collect(Collectors.toList());

        return StpSummaryDTO.builder()
                .activeStps(activeStps)
                .executingToday(executingToday)
                .expiringNext3Months(expiringNext3Months)
                .zeroBalanceCount(zeroBalanceCount)
                .monthlyTrends(monthlyTrends)
                .build();
    }

    @Override
    @Transactional
    public void validateStpTransaction(Transaction transaction) {
        // Check if source fund has sufficient balance
        FundBalance sourceBalance = fundBalanceRepository
            .findByFundIdAndClientId(transaction.getFromFund(), transaction.getClient().getId())
            .orElseThrow(() -> new InvalidTransactionException("Source fund balance not found"));

        if (sourceBalance.getBalance().compareTo(transaction.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in source fund");
        }

        if (transaction.getEndDate().isBefore(LocalDate.now())) {
            throw new InvalidTransactionException("STP end date cannot be in the past");
        }
    }

    @Override
    @Transactional
    public void processStpTransaction(Transaction transaction) {
        validateStpTransaction(transaction);
        
        // Update source fund balance
        FundBalance sourceBalance = fundBalanceRepository
            .findByFundIdAndClientId(transaction.getFromFund(), transaction.getClient().getId())
            .orElseThrow(() -> new InvalidTransactionException("Source fund balance not found"));
        
        sourceBalance.setBalance(sourceBalance.getBalance().subtract(transaction.getAmount()));
        sourceBalance.setAsOfDate(LocalDate.now());
        fundBalanceRepository.save(sourceBalance);

        // Update or create target fund balance
        FundBalance targetBalance = fundBalanceRepository
            .findByFundIdAndClientId(transaction.getToFund(), transaction.getClient().getId())
            .orElse(FundBalance.builder()
                .fundId(transaction.getToFund())
                .client(transaction.getClient())
                .balance(BigDecimal.ZERO)
                .asOfDate(LocalDate.now())
                .build());

        targetBalance.setBalance(targetBalance.getBalance().add(transaction.getAmount()));
        targetBalance.setAsOfDate(LocalDate.now());
        fundBalanceRepository.save(targetBalance);
        
        // Calculate next execution date based on frequency
        LocalDate nextDate = calculateNextExecutionDate(transaction);
        transaction.setNextTransactionDate(nextDate);
        
        // Save the transaction
        transactionRepository.save(transaction);
    }

    private LocalDate calculateNextExecutionDate(Transaction transaction) {
        LocalDate currentDate = transaction.getNextTransactionDate();
        return switch (transaction.getFrequency().toUpperCase()) {
            case "DAILY" -> currentDate.plusDays(1);
            case "WEEKLY" -> currentDate.plusWeeks(1);
            case "MONTHLY" -> currentDate.plusMonths(1);
            case "QUARTERLY" -> currentDate.plusMonths(3);
            default -> throw new InvalidTransactionException("Invalid STP frequency");
        };
    }

    @Override
    public void debugStpTrendTypes(Long userId) {
        List<Object[]> results = transactionRepository.getMonthlyStpTrendsNative(userId);
        for (Object[] row : results) {
            log.info("Month value: {}, type: {}", row[0], row[0] != null ? row[0].getClass() : "null");
            log.info("Amount value: {}, type: {}", row[1], row[1] != null ? row[1].getClass() : "null");
        }
    }
} 