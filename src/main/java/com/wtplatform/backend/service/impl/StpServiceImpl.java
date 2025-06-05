package com.wtplatform.backend.service.impl;

import com.wtplatform.backend.dto.StpSummaryDTO;
import com.wtplatform.backend.dto.StpTrendDTO;
import com.wtplatform.backend.dto.StpTransactionDTO;
import com.wtplatform.backend.exception.InsufficientBalanceException;
import com.wtplatform.backend.exception.InvalidTransactionException;
import com.wtplatform.backend.model.Transaction;
import com.wtplatform.backend.model.FundBalance;
import com.wtplatform.backend.model.User;
import com.wtplatform.backend.projection.MonthlyTrendProjection;
import com.wtplatform.backend.repository.TransactionRepository;
import com.wtplatform.backend.repository.FundBalanceRepository;
import com.wtplatform.backend.repository.UserRepository;
import com.wtplatform.backend.service.StpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StpServiceImpl implements StpService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FundBalanceRepository fundBalanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public StpSummaryDTO getStpSummaryByEmail(String email) {
        log.debug("Getting STP summary for email: {}", email);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        log.debug("Found user with ID: {}, email: {}", user.getId(), user.getEmail());
        return getStpSummary(user.getId());
    }

    @Override
    public StpSummaryDTO getStpSummary(Long userId) {
        log.debug("Starting getStpSummary calculation for userId: {}", userId);
        LocalDate today = LocalDate.now();
        LocalDate threemonthsLater = today.plusMonths(3);
        log.debug("Date range for expiring STPs: today={}, threemonthsLater={}", today, threemonthsLater);

        // Get active STPs count
        Long activeStps = transactionRepository.countActiveStpsByUserId(userId);
        log.debug("Active STPs count for userId {}: {}", userId, activeStps);

        // Get executing today count
        Long executingToday = transactionRepository.countStpsExecutingToday(userId, today);
        log.debug("STPs executing today for userId {}: {}", userId, executingToday);

        // Get expiring in next 3 months
        Long expiringNext3Months = transactionRepository.countStpsExpiringBetween(userId, today, threemonthsLater);
        log.debug("STPs expiring in next 3 months for userId {}: {}", userId, expiringNext3Months);

        // Get zero balance count
        Long zeroBalanceCount = transactionRepository.countStpsWithZeroBalance(userId);
        log.debug("STPs with zero balance for userId {}: {}", userId, zeroBalanceCount);
        
        // Get monthly trends with detailed logging
        log.debug("Fetching monthly STP trends for userId: {}", userId);
        List<MonthlyTrendProjection> trendResults = transactionRepository.getMonthlyStpTrendsNative(userId);
        log.debug("Retrieved {} monthly trend records for userId {}", trendResults.size(), userId);
        
        // Log each trend result in detail
        if (trendResults.isEmpty()) {
            log.debug("No trend results found for userId {}", userId);
        } else {
            log.debug("Detailed trend results for userId {}:", userId);
            trendResults.forEach(trend -> {
                try {
                    String month = trend.getMonth();
                    BigInteger count = trend.getCount();
                    BigDecimal amount = trend.getTotalAmount();
                    log.debug("Trend data - Month: {}, Count: {}, Amount: {}", 
                        month != null ? month : "null",
                        count != null ? count.toString() : "null",
                        amount != null ? amount : "null");
                } catch (Exception e) {
                    log.error("Error accessing trend data: {}", e.getMessage(), e);
                }
            });
        }

        // Map to DTOs with validation
        List<StpTrendDTO> monthlyTrends = trendResults.stream()
            .map(trend -> {
                StpTrendDTO dto = new StpTrendDTO(
                    trend.getMonth(),
                    trend.getTotalAmount(),
                    trend.getCount(),
                    trend.getFromFund(),
                    trend.getToFund()
                );
                log.debug("Mapped trend to DTO - Month: {}, Amount: {}, Count: {}, FromFund: {}, ToFund: {}", 
                    dto.getMonth() != null ? dto.getMonth() : "null",
                    dto.getAmount() != null ? dto.getAmount() : "null",
                    dto.getCount() != null ? dto.getCount() : "null",
                    dto.getFromFund() != null ? dto.getFromFund() : "null",
                    dto.getToFund() != null ? dto.getToFund() : "null");
                return dto;
            })
            .collect(Collectors.toList());

        log.debug("Mapped {} trend records to DTOs", monthlyTrends.size());

        // Build and validate summary
        StpSummaryDTO summary = StpSummaryDTO.builder()
                .activeStps(activeStps)
                .executingToday(executingToday)
                .expiringNext3Months(expiringNext3Months)
                .zeroBalanceCount(zeroBalanceCount)
                .monthlyTrends(monthlyTrends)
                .build();
                
        log.debug("Final STP summary for userId {}: {}", userId, summary);
        return summary;
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
        List<MonthlyTrendProjection> results = transactionRepository.getMonthlyStpTrendsNative(userId);
        for (MonthlyTrendProjection trend : results) {
            log.info("Month value: {}, Amount value: {}, Count: {}", 
                trend.getMonth(), 
                trend.getTotalAmount(),
                trend.getCount());
        }
    }

    @Override
    public List<StpTransactionDTO> getStpListByEmail(String email) {
        log.debug("Getting STP list for email: {}", email);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Transaction> stpTransactions = transactionRepository.findByClientUserIdAndType(user.getId(), "STP");
        
        return stpTransactions.stream()
            .map(txn -> StpTransactionDTO.builder()
                .id(txn.getId())
                .clientId(txn.getClient().getId().toString())
                .clientName(txn.getClient().getName())
                .amount(txn.getAmount())
                .fromFund(txn.getFromFund())
                .toFund(txn.getToFund())
                .frequency(txn.getFrequency())
                .nextTransactionDate(txn.getNextTransactionDate())
                .startDate(txn.getStartDate())
                .endDate(txn.getEndDate())
                .remainingAmount(txn.getSourceBalance())
                .sourceBalance(txn.getSourceBalance())
                .status(txn.getStatus())
                .remarks(txn.getRemarks())
                .build())
            .collect(Collectors.toList());
    }
} 