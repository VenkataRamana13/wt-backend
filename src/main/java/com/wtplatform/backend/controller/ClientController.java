package com.wtplatform.backend.controller;

import com.wtplatform.backend.dto.ClientDTO;
import com.wtplatform.backend.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientDTO> createClient(@RequestBody ClientDTO clientDTO) {
        return ResponseEntity.ok(clientService.createClient(clientDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> updateClient(@PathVariable Long id, @RequestBody ClientDTO clientDTO) {
        return ResponseEntity.ok(clientService.updateClient(id, clientDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @GetMapping("/pan/{pan}")
    public ResponseEntity<ClientDTO> getClientByPan(@PathVariable String pan) {
        return ResponseEntity.ok(clientService.getClientByPan(pan));
    }

    @GetMapping
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClientDTO>> searchClients(@RequestParam String searchTerm) {
        return ResponseEntity.ok(clientService.searchClients(searchTerm));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ClientDTO>> getClientsByRiskProfileAndHorizon(
            @RequestParam String riskProfile,
            @RequestParam String investmentHorizon) {
        return ResponseEntity.ok(clientService.getClientsByRiskProfileAndHorizon(riskProfile, investmentHorizon));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateClient(@PathVariable Long id) {
        clientService.deactivateClient(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateClient(@PathVariable Long id) {
        clientService.activateClient(id);
        return ResponseEntity.ok().build();
    }
} 