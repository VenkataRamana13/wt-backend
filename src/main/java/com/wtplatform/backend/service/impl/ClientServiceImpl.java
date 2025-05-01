package com.wtplatform.backend.service.impl;

import com.wtplatform.backend.dto.ClientDTO;
import com.wtplatform.backend.model.Client;
import com.wtplatform.backend.repository.ClientRepository;
import com.wtplatform.backend.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;

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
    }
} 