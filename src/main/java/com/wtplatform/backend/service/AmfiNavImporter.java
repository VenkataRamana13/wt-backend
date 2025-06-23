package com.wtplatform.backend.service;

import com.wtplatform.backend.model.NavHistory;
import com.wtplatform.backend.model.AmfiScheme;
import com.wtplatform.backend.repository.NavHistoryRepository;
import com.wtplatform.backend.repository.AmfiSchemeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private final ResourceLoader resourceLoader;
    
    @Value("${amfi.batch.size:1000}")
    private int batchSize;
    
    @Value("${amfi.connection.timeout:30000}")
    private int connectionTimeout;
    
    @Value("${amfi.read.timeout:60000}")
    private int readTimeout;

    private String currentAmcName;
    private String currentCategory;

    @Scheduled(cron = "0 30 23 * * *", zone = "Asia/Kolkata") // every day at 7:26 PM IST
    public void fetchAndProcessNavFile() {
        log.info("[AMFI-TXT] Scheduled AMFI import started at {}", LocalDateTime.now());
        importAmfiData();
    }

    @Transactional
    public void importAmfiData() {
        HttpURLConnection connection = null;
        StringBuilder amfiDataBuilder = new StringBuilder();
        
        try {
            // Step 1: Download and save file first
            downloadAndSaveAmfiFile(amfiDataBuilder);
            
            // Step 2: Process data and update DB
            processAmfiDataAndUpdateDb(amfiDataBuilder.toString());
            
        } catch (Exception e) {
            log.error("Error during AMFI data import: {}", e.getMessage(), e);
        }
    }
    
    private void downloadAndSaveAmfiFile(StringBuilder amfiDataBuilder) {
        HttpURLConnection connection = null;
        try {
            log.info("[AMFI-TXT] Starting AMFI file download process");
            URL url = new URL("https://www.amfiindia.com/spages/NAVAll.txt");
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectionTimeout);
            connection.setReadTimeout(readTimeout);
            
            int responseCode = connection.getResponseCode();
            log.info("[AMFI-TXT] AMFI URL connection established. Response code: {}", responseCode);
            
            // Read all data from URL
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                int lineCount = 0;
                while ((line = reader.readLine()) != null) {
                    amfiDataBuilder.append(line).append("\n");
                    lineCount++;
                }
                log.info("[AMFI-TXT] Successfully read {} lines from AMFI URL", lineCount);
            }
            
            // Save file immediately after download
            log.info("[AMFI-TXT] Attempting to save downloaded AMFI data to amfi.txt");
            saveAmfiDataToFile(amfiDataBuilder.toString());
            
        } catch (IOException e) {
            log.error("[AMFI-TXT] Error downloading AMFI data: {}. Stack trace: {}", e.getMessage(), e.getStackTrace());
            throw new RuntimeException("Failed to download AMFI data", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
                log.debug("[AMFI-TXT] AMFI URL connection closed");
            }
        }
    }
    
    private void processAmfiDataAndUpdateDb(String amfiData) {
        try {
            String[] lines = amfiData.split("\n");
            int processedCount = 0;
            LocalDate today = LocalDate.now();
            List<NavHistory> navBatch = new ArrayList<>();
            Map<String, NavUpdateInfo> latestNavs = new HashMap<>();

            for (String line : lines) {
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

                try {
                    String[] fields = line.split(";");
                    if (fields.length < 6 || fields[0].equalsIgnoreCase("Scheme Code")) {
                        continue;
                    }
                    
                    // Process the line using existing method
                    processLine(fields, navBatch, latestNavs);
                    processedCount++;

                    // Process in batches
                    if (navBatch.size() >= batchSize) {
                        saveNavBatch(navBatch, latestNavs);
                        navBatch.clear();
                        latestNavs.clear();
                    }
                } catch (Exception e) {
                    log.error("Error processing AMFI line: {}. Error: {}", line, e.getMessage());
                    // Continue processing other lines even if one fails
                    continue;
                }
            }
            
            // Process remaining records
            if (!navBatch.isEmpty()) {
                saveNavBatch(navBatch, latestNavs);
            }
            
            log.info("Successfully processed {} AMFI entries", processedCount);
            
        } catch (Exception e) {
            log.error("Error processing AMFI data for DB update: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process AMFI data for DB update", e);
        }
    }

    private void processLine(String[] fields, List<NavHistory> navBatch, Map<String, NavUpdateInfo> latestNavs) {
        String schemeCode = fields[0].trim();
        String growthOrPayoutIsin = fields[1].trim();
        String drIsin = fields.length > 2 ? fields[2].trim() : "";
        String schemeName = fields[3].trim();
        BigDecimal nav = new BigDecimal(fields[4].trim());
        LocalDate navDate = LocalDate.parse(fields[5].trim(), DateTimeFormatter.ofPattern("dd-MMM-yyyy"));

        String isin;
        String effectiveSchemeName = schemeName;

        if (growthOrPayoutIsin != null && !growthOrPayoutIsin.isEmpty() && !growthOrPayoutIsin.equals("-")) {
            isin = growthOrPayoutIsin;
        } else if (drIsin != null && !drIsin.isEmpty() && !drIsin.equals("-")) {
            isin = drIsin;
            if (!schemeName.toUpperCase().contains("DIVIDEND REINVESTMENT") &&
                !schemeName.toUpperCase().contains("DIV REINVEST") &&
                !schemeName.toUpperCase().contains("(DR)") &&
                !schemeName.toUpperCase().endsWith("-DR")) {
                effectiveSchemeName = schemeName + " - Dividend Reinvestment";
            }
        } else {
            isin = "";
        }

        log.debug("Processing line - SchemeCode: {}, Name: {}, NAV: {}, Date: {}", schemeCode, effectiveSchemeName, nav, navDate);

        // Create or update scheme
        createOrUpdateScheme(schemeCode, effectiveSchemeName, isin, nav, navDate);

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
                    log.info("Creating new scheme: {} with ISIN: {}", schemeCode, isin);
                    return newScheme;
                });

            // Update scheme information
            scheme.setSchemeName(schemeName);
            
            // Apply ISIN selection logic
            if (shouldUpdateISIN(scheme.getIsin(), isin, scheme.getSchemeName(), schemeName)) {
                log.info("Updating ISIN for scheme {}: {} -> {} (old scheme name: {}, new scheme name: {})", 
                    schemeCode, scheme.getIsin(), isin, scheme.getSchemeName(), schemeName);
                scheme.setIsin(isin);
            }
            
            scheme.setLastNavValue(nav);
            scheme.setLastNavDate(navDate);
            scheme.setAmcName(currentAmcName);
            scheme.setCategory(currentCategory);
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

    private boolean isGrowthISIN(String schemeName) {
        String upperName = schemeName.toUpperCase();
        return upperName.contains("GROWTH") || upperName.contains("(G)") || upperName.endsWith("-G");
    }

    private boolean isDividendReinvestmentISIN(String schemeName) {
        String upperName = schemeName.toUpperCase();
        return upperName.contains("DIVIDEND REINVESTMENT") || 
               upperName.contains("DIV REINVEST") || 
               upperName.contains("(DR)") || 
               upperName.endsWith("-DR");
    }

    private boolean shouldUpdateISIN(String existingISIN, String newISIN, String existingSchemeName, String newSchemeName) {
        // If no existing ISIN, update with any non-empty ISIN (prioritize growth)
        if (existingISIN == null || existingISIN.isEmpty()) {
            if (newISIN != null && !newISIN.isEmpty()) {
                return true;
            }
            return false;
        }

        // If new ISIN is empty, don't update
        if (newISIN == null || newISIN.isEmpty()) {
            return false;
        }

        boolean existingIsGrowth = isGrowthISIN(existingSchemeName);
        boolean newIsGrowth = isGrowthISIN(newSchemeName);
        boolean existingIsDR = isDividendReinvestmentISIN(existingSchemeName);
        boolean newIsDR = isDividendReinvestmentISIN(newSchemeName);

        // If existing is Growth and new is Growth, update
        if (existingIsGrowth && newIsGrowth) {
            return true;
        }

        // If existing is DR and new is Growth, update to Growth
        if (existingIsDR && newIsGrowth) {
            return true;
        }

        // If existing is DR and new is DR, update
        if (existingIsDR && newIsDR) {
            return true;
        }

        // If no existing type (neither Growth nor DR) and new has a type, update
        if (!existingIsGrowth && !existingIsDR && (newIsGrowth || newIsDR)) {
            return true;
        }

        // Keep existing ISIN in all other cases
        return false;
    }

    private void saveAmfiDataToFile(String amfiData) {
        try {
            log.info("Starting AMFI file save process");
            Resource resource = resourceLoader.getResource("classpath:amfi.txt");
            File amfiFile = resource.getFile();
            
            log.info("AMFI file path resolved to: {}", amfiFile.getAbsolutePath());
            
            // Create parent directories if they don't exist
            File parentDir = amfiFile.getParentFile();
            if (!parentDir.exists()) {
                log.info("Creating parent directory: {}", parentDir.getAbsolutePath());
                if (parentDir.mkdirs()) {
                    log.info("Successfully created parent directory");
                } else {
                    log.warn("Failed to create parent directory");
                }
            }
            
            // Check if file exists
            if (amfiFile.exists()) {
                log.info("Existing amfi.txt file found. Size: {} bytes", amfiFile.length());
            }
            
            // Write new data to file
            try (FileWriter writer = new FileWriter(amfiFile)) {
                writer.write(amfiData);
                log.info("Successfully wrote {} characters to amfi.txt", amfiData.length());
            }
            
            // Verify file was written
            if (amfiFile.exists()) {
                log.info("Verified amfi.txt file exists after save. New size: {} bytes", amfiFile.length());
            } else {
                log.warn("amfi.txt file not found after save attempt!");
            }
            
        } catch (IOException e) {
            log.error("Error saving AMFI data to file. Message: {}. Stack trace: {}", 
                e.getMessage(), e.getStackTrace());
            throw new RuntimeException("Failed to save AMFI data to file", e);
        }
    }
} 