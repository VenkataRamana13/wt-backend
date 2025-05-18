package com.wtplatform.backend.service.impl;

import com.wtplatform.backend.dto.ClientDTO;
import com.wtplatform.backend.dto.ClientDocumentDTO;
import com.wtplatform.backend.dto.DocumentUploadResponse;
import com.wtplatform.backend.model.Client;
import com.wtplatform.backend.model.User;
import com.wtplatform.backend.repository.ClientRepository;
import com.wtplatform.backend.repository.UserRepository;
import com.wtplatform.backend.service.ClientService;
import com.wtplatform.backend.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Get the currently authenticated user
     * 
     * @return the current user
     * @throws RuntimeException if no user is authenticated or user not found
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }
        
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
    
    /**
     * Verify the client belongs to the current user
     * 
     * @param clientId the client ID
     * @return the client if it belongs to the current user
     * @throws RuntimeException if the client doesn't belong to the current user
     */
    private Client verifyClientOwnership(Long clientId) {
        User currentUser = getCurrentUser();
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        
        if (!client.getUser().getId().equals(currentUser.getId())) {
            logger.warn("User {} attempted to access client {} which belongs to user {}", 
                    currentUser.getId(), clientId, client.getUser().getId());
            throw new RuntimeException("Access denied: Client does not belong to current user");
        }
        
        return client;
    }

    @Override
    @Transactional
    public ClientDTO createClient(ClientDTO clientDTO) {
        Client client = new Client();
        mapDTOToEntity(clientDTO, client);
        client.setActive(true);
        
        // Set the current user as the client's owner
        client.setUser(getCurrentUser());
        
        return mapEntityToDTO(clientRepository.save(client));
    }

    @Override
    @Transactional
    public ClientDTO updateClient(Long id, ClientDTO clientDTO) {
        // Verify client belongs to current user
        Client client = verifyClientOwnership(id);
        mapDTOToEntity(clientDTO, client);
        return mapEntityToDTO(clientRepository.save(client));
    }

    @Override
    public ClientDTO getClientById(Long id) {
        // Verify client belongs to current user
        Client client = verifyClientOwnership(id);
        return mapEntityToDTO(client);
    }

    @Override
    public ClientDTO getClientByPan(String pan) {
        User currentUser = getCurrentUser();
        return clientRepository.findByPan(pan)
                .filter(client -> client.getUser().getId().equals(currentUser.getId()))
                .map(this::mapEntityToDTO)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }

    @Override
    public List<ClientDTO> getAllClients() {
        User currentUser = getCurrentUser();
        return clientRepository.findByUserId(currentUser.getId()).stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClientDTO> searchClients(String searchTerm) {
        User currentUser = getCurrentUser();
        return clientRepository.searchClientsByUser(currentUser.getId(), searchTerm).stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClientDTO> getClientsByRiskProfileAndHorizon(String riskProfile, String investmentHorizon) {
        User currentUser = getCurrentUser();
        return clientRepository.findByRiskProfileAndInvestmentHorizon(riskProfile, investmentHorizon).stream()
                .filter(client -> client.getUser().getId().equals(currentUser.getId()))
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long getClientCount() {
        User currentUser = getCurrentUser();
        return clientRepository.findByUserId(currentUser.getId()).size();
    }

    @Override
    @Transactional
    public void deactivateClient(Long id) {
        // Verify client belongs to current user
        Client client = verifyClientOwnership(id);
        client.setActive(false);
        clientRepository.save(client);
    }

    @Override
    @Transactional
    public void activateClient(Long id) {
        // Verify client belongs to current user
        Client client = verifyClientOwnership(id);
        client.setActive(true);
        clientRepository.save(client);
    }

    @Override
    @Transactional
    public DocumentUploadResponse uploadDocument(Long clientId, MultipartFile file, String documentType) throws IOException {
        // Verify client belongs to current user
        Client client = verifyClientOwnership(clientId);
        
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
        
        // Verify client belongs to current user
        verifyClientOwnership(clientId);
        
        String prefix = String.format("clients/%d/documents/", clientId);
        List<S3Object> s3Objects = s3Service.listObjects(prefix);
        
        return mapS3ObjectsToDocumentDTOs(s3Objects, clientId);
    }
    
    @Override
    public List<ClientDocumentDTO> listClientDocumentsByType(Long clientId, String documentType) {
        logger.info("Listing documents of type '{}' for client ID: {}", documentType, clientId);
        
        // Verify client belongs to current user
        verifyClientOwnership(clientId);
        
        String prefix = String.format("clients/%d/documents/%s/", clientId, documentType);
        List<S3Object> s3Objects = s3Service.listObjects(prefix);
        
        return mapS3ObjectsToDocumentDTOs(s3Objects, clientId);
    }
    
    @Override
    public Page<ClientDTO> getPagedClients(Pageable pageable, String searchTerm) {
        User currentUser = getCurrentUser();
        List<Client> clients;
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            clients = clientRepository.searchClientsByUser(currentUser.getId(), searchTerm);
        } else {
            clients = clientRepository.findByUserId(currentUser.getId());
        }
        
        // Manual pagination (not ideal but works for this example)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), clients.size());
        
        List<ClientDTO> clientDTOs = clients.subList(start, end).stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(clientDTOs, pageable, clients.size());
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

    /**
     * Map a Client entity to a ClientDTO
     */
    private ClientDTO mapEntityToDTO(Client client) {
        return ClientDTO.builder()
                .id(client.getId())
                .name(client.getName())
                .pan(client.getPan())
                .email(client.getEmail())
                .phone(client.getPhone())
                .aum(client.getAum())
                .address(client.getAddress())
                .city(client.getCity())
                .state(client.getState())
                .pincode(client.getPincode())
                .riskProfile(client.getRiskProfile())
                .investmentHorizon(client.getInvestmentHorizon())
                .isActive(client.isActive())
                .build();
    }

    /**
     * Map a ClientDTO to a Client entity
     */
    private void mapDTOToEntity(ClientDTO dto, Client client) {
        client.setName(dto.getName());
        client.setPan(dto.getPan());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        client.setAum(dto.getAum());
        client.setAddress(dto.getAddress());
        client.setCity(dto.getCity());
        client.setState(dto.getState());
        client.setPincode(dto.getPincode());
        client.setRiskProfile(dto.getRiskProfile());
        client.setInvestmentHorizon(dto.getInvestmentHorizon());
        client.setActive(dto.isActive());
    }
    
    @Override
    @Transactional
    public List<ClientDTO> importClientsFromCSV(MultipartFile file) throws IOException {
        logger.debug("Importing clients from CSV file");
        
        // Get current user
        User currentUser = getCurrentUser();
        
        // Read CSV content
        String content = new String(file.getBytes());
        String[] lines = content.split("\n");
        
        // Check if file is empty
        if (lines.length <= 1) {
            throw new IOException("CSV file is empty or contains only headers");
        }
        
        // Parse header line
        String[] headers = lines[0].trim().split(",");
        
        // Required columns for client import
        List<String> requiredColumns = Arrays.asList(
                "name", "pan", "email", "phone", "aum", "address", 
                "city", "state", "pincode", "riskProfile", "investmentHorizon"
        );
        
        // Validate headers
        for (String required : requiredColumns) {
            boolean found = false;
            for (String header : headers) {
                if (header.trim().equalsIgnoreCase(required)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IOException("Missing required column: " + required);
            }
        }
        
        List<ClientDTO> importedClients = new ArrayList<>();
        
        // Process each data row (starting from index 1 to skip header)
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue; // Skip empty lines
            
            String[] values = line.split(",");
            
            // Validate row has correct number of columns
            if (values.length != headers.length) {
                throw new IOException("Line " + (i + 1) + " has incorrect number of values");
            }
            
            // Create map of column name to value
            Map<String, String> rowData = new HashMap<>();
            for (int j = 0; j < headers.length; j++) {
                rowData.put(headers[j].trim().toLowerCase(), values[j].trim());
            }
            
            // Create ClientDTO from row data
            ClientDTO clientDTO = new ClientDTO();
            clientDTO.setName(rowData.get("name"));
            clientDTO.setPan(rowData.get("pan"));
            clientDTO.setEmail(rowData.get("email"));
            clientDTO.setPhone(rowData.get("phone"));
            
            try {
                clientDTO.setAum(Double.parseDouble(rowData.get("aum")));
            } catch (NumberFormatException e) {
                throw new IOException("Invalid AUM value on line " + (i + 1) + ": " + rowData.get("aum"));
            }
            
            clientDTO.setAddress(rowData.get("address"));
            clientDTO.setCity(rowData.get("city"));
            clientDTO.setState(rowData.get("state"));
            clientDTO.setPincode(rowData.get("pincode"));
            clientDTO.setRiskProfile(rowData.get("riskprofile"));
            clientDTO.setInvestmentHorizon(rowData.get("investmenthorizon"));
            clientDTO.setActive(true);
            
            // Create and save the client
            try {
                // Create new client entity
                Client client = new Client();
                mapDTOToEntity(clientDTO, client);
                client.setActive(true);
                client.setUser(currentUser);
                
                // Check if client with same PAN or email already exists
                if (clientRepository.findByPan(client.getPan()).isPresent()) {
                    throw new IOException("Client with PAN " + client.getPan() + " already exists");
                }
                
                if (clientRepository.findByEmail(client.getEmail()).isPresent()) {
                    throw new IOException("Client with email " + client.getEmail() + " already exists");
                }
                
                // Save client
                Client savedClient = clientRepository.save(client);
                importedClients.add(mapEntityToDTO(savedClient));
            } catch (Exception e) {
                if (e instanceof IOException) {
                    throw e;
                }
                throw new IOException("Error importing client on line " + (i + 1) + ": " + e.getMessage());
            }
        }
        
        logger.info("Successfully imported {} clients", importedClients.size());
        return importedClients;
    }
} 