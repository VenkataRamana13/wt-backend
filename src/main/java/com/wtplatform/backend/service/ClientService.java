package com.wtplatform.backend.service;

import com.wtplatform.backend.dto.ClientDTO;
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
} 