package com.wtplatform.backend.service.impl;

import com.wtplatform.backend.dto.ClientDTO;
import com.wtplatform.backend.dto.ClientDocumentDTO;
import com.wtplatform.backend.dto.DocumentUploadResponse;
import com.wtplatform.backend.model.Client;
import com.wtplatform.backend.repository.ClientRepository;
import com.wtplatform.backend.service.ClientService;
import com.wtplatform.backend.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {
    private static final Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("(\\d{8}_\\d{6})_(.+)$");

    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private S3Service s3Service;

    @Override
    @Transactional
    public ClientDTO createClient(ClientDTO clientDTO) {
        Client client = new Client();
        mapDTOToEntity(clientDTO, client);
        client.setActive(true);
        return mapEntityToDTO(clientRepository.save(client));
    }

    @Override
    @Transactional
    public ClientDTO updateClient(Long id, ClientDTO clientDTO) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        mapDTOToEntity(clientDTO, client);
        return mapEntityToDTO(clientRepository.save(client));
    }

    @Override
    public ClientDTO getClientById(Long id) {
        return clientRepository.findById(id)
                .map(this::mapEntityToDTO)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }

    @Override
    public ClientDTO getClientByPan(String pan) {
        return clientRepository.findByPan(pan)
                .map(this::mapEntityToDTO)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }

    @Override
    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClientDTO> searchClients(String searchTerm) {
        return clientRepository.searchClients(searchTerm).stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClientDTO> getClientsByRiskProfileAndHorizon(String riskProfile, String investmentHorizon) {
        return clientRepository.findByRiskProfileAndInvestmentHorizon(riskProfile, investmentHorizon).stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deactivateClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        client.setActive(false);
        clientRepository.save(client);
    }

    @Override
    @Transactional
    public void activateClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        client.setActive(true);
        clientRepository.save(client);
    }

    @Override
    @Transactional
    public DocumentUploadResponse uploadDocument(Long clientId, MultipartFile file, String documentType) throws IOException {
        // Verify client exists
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        
        logger.info("Uploading document type '{}' for client ID: {}", documentType, clientId);
        
        // Create folder path based on document type and client ID
        String folderPath = String.format("clients/%d/documents/%s", clientId, documentType);
        
        // Upload file to S3
        String fileKey = s3Service.uploadFile(file, folderPath);
        
        logger.info("Document uploaded successfully with key: {}", fileKey);
        
        return new DocumentUploadResponse(
                "Document uploaded successfully",
                fileKey,
                true,
                documentType,
                clientId
        );
    }
    
    @Override
    public void downloadDocument(String fileKey, Path destinationPath) {
        logger.info("Downloading document with key: {} to path: {}", fileKey, destinationPath);
        s3Service.downloadFile(fileKey, destinationPath);
    }
    
    @Override
    public void deleteDocument(String fileKey) {
        logger.info("Deleting document with key: {}", fileKey);
        s3Service.deleteFile(fileKey);
    }
    
    @Override
    public List<ClientDocumentDTO> listClientDocuments(Long clientId) {
        logger.info("Listing all documents for client ID: {}", clientId);
        
        // Verify client exists
        clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        
        String prefix = String.format("clients/%d/documents/", clientId);
        List<S3Object> s3Objects = s3Service.listObjects(prefix);
        
        return mapS3ObjectsToDocumentDTOs(s3Objects, clientId);
    }
    
    @Override
    public List<ClientDocumentDTO> listClientDocumentsByType(Long clientId, String documentType) {
        logger.info("Listing documents of type '{}' for client ID: {}", documentType, clientId);
        
        // Verify client exists
        clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        
        String prefix = String.format("clients/%d/documents/%s/", clientId, documentType);
        List<S3Object> s3Objects = s3Service.listObjects(prefix);
        
        return mapS3ObjectsToDocumentDTOs(s3Objects, clientId);
    }
    
    private List<ClientDocumentDTO> mapS3ObjectsToDocumentDTOs(List<S3Object> s3Objects, Long clientId) {
        List<ClientDocumentDTO> documentDTOs = new ArrayList<>();
        
        for (S3Object s3Object : s3Objects) {
            String key = s3Object.key();
            
            // Skip folder objects (they end with /)
            if (key.endsWith("/")) {
                continue;
            }
            
            // Extract document type from the key path
            String[] pathSegments = key.split("/");
            if (pathSegments.length < 4) {
                logger.warn("Unexpected S3 key format: {}", key);
                continue;
            }
            
            String documentType = pathSegments[pathSegments.length - 2];
            String fileNameWithTimestamp = pathSegments[pathSegments.length - 1];
            
            // Extract the original filename and timestamp
            Matcher matcher = FILE_NAME_PATTERN.matcher(fileNameWithTimestamp);
            String fileName = fileNameWithTimestamp;
            Instant uploadedAt = Instant.now(); // Default to now if we can't parse
            
            if (matcher.find()) {
                String timestamp = matcher.group(1); // YYYYMMDD_HHMMSS
                fileName = matcher.group(2); // Original filename
                
                // Convert timestamp to Instant (best effort)
                try {
                    // Parse timestamp in format YYYYMMDD_HHMMSS
                    String year = timestamp.substring(0, 4);
                    String month = timestamp.substring(4, 6);
                    String day = timestamp.substring(6, 8);
                    String hour = timestamp.substring(9, 11);
                    String minute = timestamp.substring(11, 13);
                    String second = timestamp.substring(13, 15);
                    
                    String isoTimestamp = String.format("%s-%s-%sT%s:%s:%sZ", 
                            year, month, day, hour, minute, second);
                    uploadedAt = Instant.parse(isoTimestamp);
                } catch (Exception e) {
                    logger.warn("Failed to parse timestamp from filename: {}", fileNameWithTimestamp, e);
                }
            }
            
            ClientDocumentDTO documentDTO = new ClientDocumentDTO(
                    key,
                    fileName,
                    documentType,
                    clientId,
                    s3Object.size(),
                    uploadedAt
            );
            
            documentDTOs.add(documentDTO);
        }
        
        return documentDTOs;
    }

    private ClientDTO mapEntityToDTO(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setPan(client.getPan());
        dto.setEmail(client.getEmail());
        dto.setPhone(client.getPhone());
        dto.setAddress(client.getAddress());
        dto.setRiskProfile(client.getRiskProfile());
        dto.setInvestmentHorizon(client.getInvestmentHorizon());
        dto.setActive(client.isActive());
        dto.setAum(client.getAum());
        dto.setCity(client.getCity());
        dto.setState(client.getState());
        dto.setPincode(client.getPincode());
        return dto;
    }

    private void mapDTOToEntity(ClientDTO dto, Client client) {
        client.setName(dto.getName());
        client.setPan(dto.getPan());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        client.setAddress(dto.getAddress());
        client.setRiskProfile(dto.getRiskProfile());
        client.setInvestmentHorizon(dto.getInvestmentHorizon());
        client.setAum(dto.getAum());
        client.setCity(dto.getCity());
        client.setState(dto.getState());
        client.setPincode(dto.getPincode());
    }
} 