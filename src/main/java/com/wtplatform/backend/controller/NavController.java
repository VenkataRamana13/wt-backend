package com.wtplatform.backend.controller;

import com.wtplatform.backend.service.AmfiNavImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.wtplatform.backend.model.NavHistory;
import com.wtplatform.backend.repository.NavHistoryRepository;

@RestController
@RequestMapping("/api/nav")
public class NavController {
    private static final Logger logger = LoggerFactory.getLogger(NavController.class);

    @Autowired
    private AmfiNavImporter amfiNavImporter;

    @Autowired
    private NavHistoryRepository navHistoryRepo;

    @PostMapping("/import")
    public ResponseEntity<String> importNav() {
        try {
            amfiNavImporter.fetchAndProcessNavFile();
            return ResponseEntity.ok("NAV import process started successfully");
        } catch (Exception e) {
            logger.error("Error starting NAV import", e);
            return ResponseEntity.internalServerError().body("Error starting NAV import: " + e.getMessage());
        }
    }

    @PostMapping("/migrate-to-schemes")
    public ResponseEntity<String> migrateNavHistoryToSchemes() {
        try {
            amfiNavImporter.migrateNavHistoryToSchemes();
            return ResponseEntity.ok("Migration completed successfully");
        } catch (Exception e) {
            logger.error("Error during migration", e);
            return ResponseEntity.internalServerError().body("Migration failed: " + e.getMessage());
        }
    }

    @PostMapping("/history")
    public ResponseEntity<NavHistory> addNavHistory(
            @RequestParam String schemeCode,
            @RequestParam BigDecimal nav,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate navDate) {
        
        logger.debug("Adding single NAV history entry - SchemeCode: {}, Nav: {}, Date: {}", 
            schemeCode, nav, navDate);
            
        NavHistory navHistory = new NavHistory(schemeCode, navDate, nav, "AMFI");
        NavHistory saved = navHistoryRepo.save(navHistory);
        
        logger.debug("Saved NAV history entry - ID: {}, FundId: {}, NavDate: {}, Nav: {}", 
            saved.getId(), saved.getFundId(), saved.getNavDate(), saved.getNav());
            
        return ResponseEntity.ok(saved);
    }
} 