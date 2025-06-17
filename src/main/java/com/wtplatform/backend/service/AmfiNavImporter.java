package com.wtplatform.backend.service;

import com.wtplatform.backend.model.NavHistory;
import com.wtplatform.backend.model.AmfiScheme;
import com.wtplatform.backend.repository.NavHistoryRepository;
import com.wtplatform.backend.repository.AmfiSchemeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmfiNavImporter {

    private final NavHistoryRepository navRepo;
    private final AmfiSchemeRepository schemeRepo;
    
    @Value("${amfi.batch.size:1000}")
    private int batchSize;
    
    @Value("${amfi.connection.timeout:30000}")
    private int connectionTimeout;
    
    @Value("${amfi.read.timeout:60000}")
    private int readTimeout;

    private String currentAmcName;
    private String currentCategory;

    @Scheduled(cron = "0 22 22 * * *", zone = "Asia/Kolkata") // every day at 7:26 PM IST
    public void fetchAndProcessNavFile() {
        log.info("Starting AMFI NAV import process at {}", LocalDateTime.now());
        
        HttpURLConnection connection = null;
        List<NavHistory> navBatch = new ArrayList<>();
        Map<String, NavUpdateInfo> latestNavs = new HashMap<>();
        
        try {
            URL url = new URL("https://www.amfiindia.com/spages/NAVAll.txt");
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectionTimeout);
            connection.setReadTimeout(readTimeout);
            
            log.info("Established connection to AMFI URL. Response code: {}", connection.getResponseCode());
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                int processedCount = 0;
                LocalDate today = LocalDate.now();
                currentAmcName = null;
                currentCategory = null;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    
                    // Skip empty lines
                    if (line.isEmpty()) {
                        continue;
                    }
                    
                    // Check if this is an AMC name line
                    if (!line.contains(";") && line.contains("Mutual Fund")) {
                        currentAmcName = line.trim();
                        continue;
                    }
                    
                    // Check if this is a category line (section header)
                    if (!line.contains(";") && line.contains("Open Ended Schemes")) {
                        currentCategory = line.trim();
                        continue;
                    }
                    
                    // Skip header and section header lines
                    if (!line.contains(";") || line.equalsIgnoreCase("Scheme Code")) {
                        continue;
                    }

                    String[] fields = line.split(";");
                    if (fields.length < 6 || fields[0].equalsIgnoreCase("Scheme Code")) {
                        continue;
                    }

                    try {
                        processLine(fields, navBatch, latestNavs);
                        processedCount++;

                        // Process in batches
                        if (navBatch.size() >= batchSize) {
                            saveNavBatch(navBatch, latestNavs);
                            navBatch.clear();
                            latestNavs.clear();
                        }
                    } catch (Exception e) {
                        log.error("Error processing line: " + line, e);
                    }
                }

                // Process remaining records
                if (!navBatch.isEmpty()) {
                    saveNavBatch(navBatch, latestNavs);
                }

                log.info("Completed AMFI NAV import process. Processed {} records", processedCount);
            }
        } catch (Exception e) {
            log.error("Error in AMFI NAV import process", e);
            throw new RuntimeException("Failed to import AMFI NAV data", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void processLine(String[] fields, List<NavHistory> navBatch, Map<String, NavUpdateInfo> latestNavs) {
        String schemeCode = fields[0].trim();
        String isin = fields[1].trim();
        String schemeName = fields[3].trim();
        BigDecimal nav = new BigDecimal(fields[4].trim());
        LocalDate navDate = LocalDate.parse(fields[5].trim(), DateTimeFormatter.ofPattern("dd-MMM-yyyy"));

        log.debug("Processing line - SchemeCode: {}, Name: {}, NAV: {}, Date: {}", schemeCode, schemeName, nav, navDate);

        // Create or update scheme
        createOrUpdateScheme(schemeCode, schemeName, isin, nav, navDate);

        // Add NAV history
        NavHistory navHistory = new NavHistory(schemeCode, navDate, nav, "AMFI");
        navBatch.add(navHistory);
        log.debug("Added to navBatch - SchemeCode: {}, Size now: {}", schemeCode, navBatch.size());

        // Keep track of latest NAV for each scheme
        NavUpdateInfo existing = latestNavs.get(schemeCode);
        if (existing == null || navDate.isAfter(existing.date)) {
            latestNavs.put(schemeCode, new NavUpdateInfo(nav, navDate));
            log.debug("Updated latestNavs for scheme: {} with date: {}", schemeCode, navDate);
        }
    }

    private void createOrUpdateScheme(String schemeCode, String schemeName, String isin, BigDecimal nav, LocalDate navDate) {
        try {
            AmfiScheme scheme = schemeRepo.findById(schemeCode)
                .orElseGet(() -> {
                    // Create new scheme if it doesn't exist
                    AmfiScheme newScheme = new AmfiScheme();
                    newScheme.setSchemeCode(schemeCode);
                    newScheme.setSchemeName(schemeName);
                    newScheme.setIsin(isin);
                    newScheme.setIsActive(true);
                    log.info("Creating new scheme: {}", schemeCode);
                    return newScheme;
                });

            // Update scheme information
            scheme.setSchemeName(schemeName);
            scheme.setIsin(isin);
            scheme.setLastNavValue(nav);
            scheme.setLastNavDate(navDate);
            scheme.setAmcName(currentAmcName); // Set the current AMC name
            scheme.setCategory(currentCategory); // Set the current category
            schemeRepo.save(scheme);
        } catch (Exception e) {
            log.error("Error creating/updating scheme {}: {}", schemeCode, e.getMessage());
        }
    }

    @Transactional
    protected void saveNavBatch(List<NavHistory> navBatch, Map<String, NavUpdateInfo> latestNavs) {
        if (navBatch.isEmpty()) {
            return;
        }

        log.info("Processing batch of {} NAV entries", navBatch.size());
        List<NavHistory> successfulSaves = new ArrayList<>();
        Map<String, String> failedEntries = new HashMap<>();

        // Process each NAV entry individually within the batch
        for (NavHistory nav : navBatch) {
            try {
                NavHistory savedNav = navRepo.save(nav);
                successfulSaves.add(savedNav);
                log.debug("Successfully saved NAV entry - FundId: {}, Date: {}, Nav: {}", 
                    nav.getFundId(), nav.getNavDate(), nav.getNav());
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                failedEntries.put(nav.getFundId() + "_" + nav.getNavDate(), errorMessage);
                log.warn("Failed to save NAV entry - FundId: {}, Date: {}, Error: {}", 
                    nav.getFundId(), nav.getNavDate(), errorMessage);
            }
        }

        // Log summary of the batch processing
        log.info("Batch processing complete - Success: {}, Failed: {}", 
            successfulSaves.size(), failedEntries.size());

        if (!failedEntries.isEmpty()) {
            log.warn("Failed entries summary:");
            failedEntries.forEach((key, error) -> 
                log.warn("Entry: {}, Error: {}", key, error));
        }

        // Update AMFI schemes with latest NAVs for successful entries
        updateAmfiSchemesWithLatestNavs(successfulSaves, latestNavs);
    }

    private void updateAmfiSchemesWithLatestNavs(List<NavHistory> successfulSaves, Map<String, NavUpdateInfo> latestNavs) {
        log.info("Updating AMFI schemes with latest NAVs for {} entries", latestNavs.size());
        int updatedSchemes = 0;
        
        for (NavHistory nav : successfulSaves) {
            String schemeCode = nav.getFundId().toString();
            NavUpdateInfo updateInfo = latestNavs.get(schemeCode);
            
            if (updateInfo != null && updateInfo.date.equals(nav.getNavDate())) {
                try {
                    schemeRepo.updateNav(schemeCode, updateInfo.nav, updateInfo.date);
                    updatedSchemes++;
                    log.debug("Updated AMFI scheme - Code: {}, Date: {}, NAV: {}", 
                        schemeCode, updateInfo.date, updateInfo.nav);
                } catch (Exception e) {
                    log.error("Failed to update AMFI scheme {}: {}", schemeCode, e.getMessage());
                }
            }
        }
        
        log.info("Successfully updated {}/{} AMFI schemes with latest NAV", 
            updatedSchemes, latestNavs.size());
    }

    @Transactional
    public void migrateNavHistoryToSchemes() {
        log.info("Starting migration from nav_history to amfi_schemes");
        try {
            // Get all unique scheme codes and their latest NAVs from nav_history
            List<Object[]> latestNavs = navRepo.findLatestNavForAllSchemes();
            int count = 0;

            for (Object[] result : latestNavs) {
                Long fundId = (Long) result[0];
                BigDecimal nav = (BigDecimal) result[1];
                LocalDate navDate = (LocalDate) result[2];
                String source = (String) result[3];

                String schemeCode = String.valueOf(fundId);
                AmfiScheme scheme = schemeRepo.findById(schemeCode)
                    .orElseGet(() -> {
                        AmfiScheme newScheme = new AmfiScheme();
                        newScheme.setSchemeCode(schemeCode);
                        newScheme.setSchemeName("Scheme " + schemeCode); // Default name
                        newScheme.setIsActive(true);
                        log.info("Creating new scheme: {}", schemeCode);
                        return newScheme;
                    });

                scheme.setLastNavValue(nav);
                scheme.setLastNavDate(navDate);
                schemeRepo.save(scheme);
                count++;

                if (count % 100 == 0) {
                    log.info("Processed {} schemes", count);
                }
            }
            log.info("Completed migration. Total schemes processed: {}", count);
        } catch (Exception e) {
            log.error("Error during migration", e);
            throw e;
        }
    }

    private static class NavUpdateInfo {
        final BigDecimal nav;
        final LocalDate date;

        NavUpdateInfo(BigDecimal nav, LocalDate date) {
            this.nav = nav;
            this.date = date;
        }
    }
} 