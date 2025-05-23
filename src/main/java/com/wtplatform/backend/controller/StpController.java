package com.wtplatform.backend.controller;

import com.wtplatform.backend.dto.StpSummaryDTO;
import com.wtplatform.backend.service.StpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stp")
@RequiredArgsConstructor
public class StpController {

    private final StpService stpService;

    @GetMapping("/summary")
    public ResponseEntity<StpSummaryDTO> getStpSummary(@RequestParam Long userId) {
        return ResponseEntity.ok(stpService.getStpSummary(userId));
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
} 