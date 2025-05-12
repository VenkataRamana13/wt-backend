package com.wtplatform.backend.service.impl;

import com.wtplatform.backend.dto.DashboardStatsDTO;
import com.wtplatform.backend.repository.ClientRepository;
import com.wtplatform.backend.service.ClientService;
import com.wtplatform.backend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private ClientService clientService;
    
    @Override
    public DashboardStatsDTO getDashboardStats() {
        // Get user-specific client count
        long clientCount = clientService.getClientCount();
        
        // Calculate total AUM for current user by summing their clients' AUM values
        double totalAum = clientService.getAllClients().stream()
                .mapToDouble(clientDTO -> clientDTO.getAum())
                .sum();
        
        return DashboardStatsDTO.builder()
                .clientCount(clientCount)
                .totalAum(totalAum)
                .build();
    }
} 