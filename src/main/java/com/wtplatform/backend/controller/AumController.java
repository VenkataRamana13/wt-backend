package com.wtplatform.backend.controller;

import com.wtplatform.backend.dto.AumSummaryDTO;
import com.wtplatform.backend.dto.AumTrendDTO;
import com.wtplatform.backend.dto.AumBreakdownDTO;
import com.wtplatform.backend.service.AumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/aum")
public class AumController {

    private final AumService aumService;

    public AumController(AumService aumService) {
        this.aumService = aumService;
    }

    @GetMapping("/summary")
    public ResponseEntity<AumSummaryDTO> getAumSummary() {
        return ResponseEntity.ok(aumService.getAumSummary());
    }

    @GetMapping("/trend")
    public ResponseEntity<List<AumTrendDTO>> getAumTrend(
            @RequestParam(defaultValue = "1m") String period) {
        return ResponseEntity.ok(aumService.getAumTrend(period));
    }

    @GetMapping("/breakdown")
    public ResponseEntity<AumBreakdownDTO> getAumBreakdown() {
        return ResponseEntity.ok(aumService.getAumBreakdown());
    }
} 