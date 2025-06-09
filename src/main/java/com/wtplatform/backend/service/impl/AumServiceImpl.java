package com.wtplatform.backend.service.impl;

import com.wtplatform.backend.dto.AumSummaryDTO;
import com.wtplatform.backend.dto.AumTrendDTO;
import com.wtplatform.backend.dto.AumBreakdownDTO;
import com.wtplatform.backend.service.AumService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.time.temporal.ChronoUnit;

@Service
public class AumServiceImpl implements AumService {

    @Override
    public AumSummaryDTO getAumSummary() {
        // TODO: Implement actual data fetching from database
        return AumSummaryDTO.builder()
                .totalAum(10000000.0)  // Example value
                .contributingClients(50)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Override
    public List<AumTrendDTO> getAumTrend(String period) {
        List<AumTrendDTO> trendData = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(period);

        // TODO: Replace with actual data from database
        double baseAum = 9000000.0;
        Random random = new Random();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // Simulate some random fluctuation in AUM
            double fluctuation = 1.0 + (random.nextDouble() * 0.02 - 0.01); // ±1% change
            baseAum *= fluctuation;
            
            trendData.add(AumTrendDTO.builder()
                    .date(currentDate)
                    .aumValue(baseAum)
                    .changePercentage((fluctuation - 1.0) * 100)
                    .build());
            
            currentDate = currentDate.plusDays(1);
        }

        return trendData;
    }

    @Override
    public AumBreakdownDTO getAumBreakdown() {
        // TODO: Implement actual data fetching from database
        Map<String, Double> byClient = new HashMap<>();
        byClient.put("Client A", 2000000.0);
        byClient.put("Client B", 3000000.0);
        byClient.put("Client C", 5000000.0);

        Map<String, Double> byAssetClass = new HashMap<>();
        byAssetClass.put("Equity", 4000000.0);
        byAssetClass.put("Fixed Income", 3000000.0);
        byAssetClass.put("Real Estate", 2000000.0);
        byAssetClass.put("Alternative Investments", 1000000.0);

        Map<String, Double> byTimeSegment = new HashMap<>();
        byTimeSegment.put("1M", 9800000.0);
        byTimeSegment.put("3M", 9500000.0);
        byTimeSegment.put("6M", 9000000.0);
        byTimeSegment.put("1Y", 8000000.0);

        return AumBreakdownDTO.builder()
                .byClient(byClient)
                .byAssetClass(byAssetClass)
                .byTimeSegment(byTimeSegment)
                .build();
    }

    private LocalDate calculateStartDate(String period) {
        LocalDate endDate = LocalDate.now();
        return switch (period.toLowerCase()) {
            case "1m" -> endDate.minus(1, ChronoUnit.MONTHS);
            case "3m" -> endDate.minus(3, ChronoUnit.MONTHS);
            case "6m" -> endDate.minus(6, ChronoUnit.MONTHS);
            case "1y" -> endDate.minus(1, ChronoUnit.YEARS);
            default -> endDate.minus(1, ChronoUnit.MONTHS); // Default to 1 month
        };
    }
} 