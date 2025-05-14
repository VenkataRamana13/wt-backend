package com.wtplatform.backend.service.impl;

import com.wtplatform.backend.dto.DashboardStatsDTO;
import com.wtplatform.backend.dto.TransactionDTO;
import com.wtplatform.backend.repository.ClientRepository;
import com.wtplatform.backend.service.ClientService;
import com.wtplatform.backend.service.DashboardService;
import com.wtplatform.backend.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_TRANSACTIONS_LIMIT = 4;

    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private ClientService clientService;
    
    @Autowired
    private TransactionService transactionService;
    
    @Override
    public DashboardStatsDTO getDashboardStats() {
        // Get user-specific client count
        long clientCount = clientService.getClientCount();
        
        // Calculate total AUM for current user by summing their clients' AUM values
        double totalAum = clientService.getAllClients().stream()
                .mapToDouble(clientDTO -> clientDTO.getAum())
                .sum();
        
        // Get most recent transactions for the current user
        List<TransactionDTO> recentTransactions = transactionService.getRecentTransactions(RECENT_TRANSACTIONS_LIMIT);
        
        return DashboardStatsDTO.builder()
                .clientCount(clientCount)
                .totalAum(totalAum)
                .recentTransactions(recentTransactions)
                .build();
    }
} 