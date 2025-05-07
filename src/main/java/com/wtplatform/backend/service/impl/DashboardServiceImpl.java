package com.wtplatform.backend.service.impl;

import com.wtplatform.backend.dto.DashboardStatsDTO;
import com.wtplatform.backend.repository.ClientRepository;
import com.wtplatform.backend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ClientRepository clientRepository;
    
    @Override
    public DashboardStatsDTO getDashboardStats() {
        long clientCount = clientRepository.count();
        
        // Calculate total AUM by summing all clients' AUM values
        double totalAum = clientRepository.findAll().stream()
                .mapToDouble(client -> client.getAum())
                .sum();
        
        return DashboardStatsDTO.builder()
                .clientCount(clientCount)
                .totalAum(totalAum)
                .build();
    }
} 