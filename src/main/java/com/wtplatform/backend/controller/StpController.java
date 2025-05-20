package com.wtplatform.backend.controller;

import com.wtplatform.backend.dto.StpSummaryDTO;
import com.wtplatform.backend.service.StpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stp")
public class StpController {

    private static final Logger logger = LoggerFactory.getLogger(StpController.class);
    
    @Autowired
    private StpService stpService;
    
    /**
     * Get STP summary including active counts, trends, etc.
     * 
     * @param months Number of months to include in trend data (default: 6)
     * @return STP summary data
     */
    @GetMapping("/summary")
    public ResponseEntity<StpSummaryDTO> getStpSummary(
            @RequestParam(defaultValue = "6") int months) {
        
        logger.info("Received STP summary request for {} months", months);
        StpSummaryDTO summary = stpService.getStpSummary(months);
        return ResponseEntity.ok(summary);
    }
} 