package com.wtplatform.backend.controller;

import com.wtplatform.backend.dto.StpSummaryDTO;
import com.wtplatform.backend.model.Transaction;
import com.wtplatform.backend.service.StpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stp")
@RequiredArgsConstructor
public class StpController {

    private final StpService stpService;

    @GetMapping("/summary")
    public ResponseEntity<StpSummaryDTO> getStpSummary(Authentication authentication) {
        return ResponseEntity.ok(stpService.getStpSummaryByEmail(authentication.getName()));
    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validateStpTransaction(@RequestBody Transaction transaction) {
        stpService.validateStpTransaction(transaction);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/process")
    public ResponseEntity<Void> processStpTransaction(@RequestBody Transaction transaction) {
        stpService.processStpTransaction(transaction);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/debug-types/{userId}")
    public ResponseEntity<String> debugTypes(@PathVariable Long userId) {
        stpService.debugStpTrendTypes(userId);
        return ResponseEntity.ok("Check logs for type information");
    }
} 