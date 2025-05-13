package com.wtplatform.backend.controller;

import com.wtplatform.backend.dto.ClientDTO;
import com.wtplatform.backend.dto.ClientDocumentDTO;
import com.wtplatform.backend.dto.DocumentUploadResponse;
import com.wtplatform.backend.dto.ErrorResponse;
import com.wtplatform.backend.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @GetMapping("/count")
    public ResponseEntity<Long> getClientCount() {
        logger.debug("Received get client count request");
        return ResponseEntity.ok(clientService.getClientCount());
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

    @PostMapping(value = "/{clientId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long clientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType) {
        
        try {
            logger.info("Document upload request received for client ID: {}, document type: {}", clientId, documentType);
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("File cannot be empty"));
            }
            
            DocumentUploadResponse response = clientService.uploadDocument(clientId, file, documentType);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Failed to upload document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to upload document: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during document upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{clientId}/documents")
    public ResponseEntity<?> getClientDocuments(@PathVariable Long clientId) {
        try {
            logger.info("Getting documents for client ID: {}", clientId);
            List<ClientDocumentDTO> documents = clientService.listClientDocuments(clientId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            logger.error("Failed to get documents for client", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to get client documents: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{clientId}/documents/{documentType}")
    public ResponseEntity<?> getClientDocumentsByType(
            @PathVariable Long clientId,
            @PathVariable String documentType) {
        try {
            logger.info("Getting documents of type '{}' for client ID: {}", documentType, clientId);
            List<ClientDocumentDTO> documents = clientService.listClientDocumentsByType(clientId, documentType);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            logger.error("Failed to get documents for client", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to get client documents: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/documents")
    public ResponseEntity<?> deleteDocument(@RequestParam("fileKey") String fileKey) {
        try {
            logger.info("Document delete request received for key: {}", fileKey);
            clientService.deleteDocument(fileKey);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to delete document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to delete document: " + e.getMessage()));
        }
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<ClientDTO>> getPagedClients(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        logger.debug("Received paged clients request with searchTerm: {}, page: {}, size: {}", 
                searchTerm, page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ClientDTO> result = clientService.getPagedClients(pageable, searchTerm);
            // Page will always have a non-null content array (could be empty)
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error retrieving paged clients", e);
            // Return an empty page rather than an error to ensure frontend gets a valid response structure
            return ResponseEntity.ok(Page.empty(PageRequest.of(page, size)));
        }
    }
} 