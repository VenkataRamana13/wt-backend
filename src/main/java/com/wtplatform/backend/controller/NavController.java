package com.wtplatform.backend.controller;

import com.wtplatform.backend.service.AmfiNavImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nav")
public class NavController {
    private static final Logger logger = LoggerFactory.getLogger(NavController.class);

    @Autowired
    private AmfiNavImporter amfiNavImporter;

    @PostMapping("/import")
    public ResponseEntity<String> triggerNavImport() {
        logger.info("Manual NAV import triggered");
        try {
            amfiNavImporter.fetchAndProcessNavFile();
            return ResponseEntity.ok("NAV import triggered successfully");
        } catch (Exception e) {
            logger.error("Error during manual NAV import", e);
            return ResponseEntity.internalServerError().body("Error during NAV import: " + e.getMessage());
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
} 