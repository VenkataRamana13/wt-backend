package com.wtplatform.backend.controller;

import com.wtplatform.backend.dto.ClientDTO;
import com.wtplatform.backend.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Autowired
    private ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientDTO> createClient(@RequestBody ClientDTO clientDTO) {
        logger.debug("Received create client request: {}", clientDTO);
        return ResponseEntity.ok(clientService.createClient(clientDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> updateClient(@PathVariable Long id, @RequestBody ClientDTO clientDTO) {
        logger.debug("Received update client request for id {}: {}", id, clientDTO);
        return ResponseEntity.ok(clientService.updateClient(id, clientDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
        logger.debug("Received get client request for id: {}", id);
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @GetMapping("/pan/{pan}")
    public ResponseEntity<ClientDTO> getClientByPan(@PathVariable String pan) {
        logger.debug("Received get client request for PAN: {}", pan);
        return ResponseEntity.ok(clientService.getClientByPan(pan));
    }

    @GetMapping
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        logger.debug("Received get all clients request");
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClientDTO>> searchClients(@RequestParam String searchTerm) {
        logger.debug("Received search clients request with term: {}", searchTerm);
        return ResponseEntity.ok(clientService.searchClients(searchTerm));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ClientDTO>> getClientsByRiskProfileAndHorizon(
            @RequestParam String riskProfile,
            @RequestParam String investmentHorizon) {
        logger.debug("Received filter clients request with risk profile: {} and horizon: {}", riskProfile, investmentHorizon);
        return ResponseEntity.ok(clientService.getClientsByRiskProfileAndHorizon(riskProfile, investmentHorizon));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateClient(@PathVariable Long id) {
        logger.debug("Received deactivate client request for id: {}", id);
        clientService.deactivateClient(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateClient(@PathVariable Long id) {
        logger.debug("Received activate client request for id: {}", id);
        clientService.activateClient(id);
        return ResponseEntity.ok().build();
    }
} 