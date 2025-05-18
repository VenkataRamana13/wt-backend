package com.wtplatform.backend.service;

import com.wtplatform.backend.dto.ClientDTO;
import com.wtplatform.backend.dto.ClientDocumentDTO;
import com.wtplatform.backend.dto.DocumentUploadResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ClientService {
    ClientDTO createClient(ClientDTO clientDTO);
    ClientDTO updateClient(Long id, ClientDTO clientDTO);
    ClientDTO getClientById(Long id);
    ClientDTO getClientByPan(String pan);
    List<ClientDTO> getAllClients();
    List<ClientDTO> searchClients(String searchTerm);
    List<ClientDTO> getClientsByRiskProfileAndHorizon(String riskProfile, String investmentHorizon);
    void deactivateClient(Long id);
    void activateClient(Long id);
    
    /**
     * Get the total count of clients for the current authenticated user
     * 
     * @return the total number of clients for the current user
     */
    long getClientCount();
    
    /**
     * Upload a document for a client
     * Checks if the client belongs to the current user
     * 
     * @param clientId the ID of the client
     * @param file the file to upload
     * @param documentType the type of document (e.g., "kyc", "address_proof", "identity_proof")
     * @return the upload response with the document key
     * @throws IOException if there's an issue with the file
     */
    DocumentUploadResponse uploadDocument(Long clientId, MultipartFile file, String documentType) throws IOException;
    
    /**
     * Download a client document to the specified path
     * 
     * @param fileKey the S3 key of the document
     * @param destinationPath the path to save the file to
     */
    void downloadDocument(String fileKey, Path destinationPath);
    
    /**
     * Delete a client document
     * 
     * @param fileKey the S3 key of the document to delete
     */
    void deleteDocument(String fileKey);
    
    /**
     * List all documents for a client
     * Checks if the client belongs to the current user
     * 
     * @param clientId the ID of the client
     * @return a list of client documents
     */
    List<ClientDocumentDTO> listClientDocuments(Long clientId);
    
    /**
     * List all documents for a client with a specific document type
     * Checks if the client belongs to the current user
     * 
     * @param clientId the ID of the client
     * @param documentType the type of document
     * @return a list of client documents of the specified type
     */
    List<ClientDocumentDTO> listClientDocumentsByType(Long clientId, String documentType);
    
    /**
     * Get a paginated list of clients for the current authenticated user
     * 
     * @param pageable the pagination information
     * @param searchTerm optional search term
     * @return a page of clients for the current user
     */
    Page<ClientDTO> getPagedClients(Pageable pageable, String searchTerm);
    
    /**
     * Import clients from a CSV file
     * Each row in the CSV will be converted to a client record
     * 
     * @param file the CSV file to import
     * @return a list of the imported clients
     * @throws IOException if there's an issue with the file
     */
    List<ClientDTO> importClientsFromCSV(MultipartFile file) throws IOException;
} 