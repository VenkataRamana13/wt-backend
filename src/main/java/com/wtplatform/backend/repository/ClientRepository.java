package com.wtplatform.backend.repository;

import com.wtplatform.backend.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByPan(String pan);
    Optional<Client> findByEmail(String email);
    List<Client> findByIsActive(boolean isActive);
    
    @Query("SELECT c FROM Client c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.pan) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Client> searchClients(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT c FROM Client c WHERE " +
           "c.riskProfile = :riskProfile AND " +
           "c.investmentHorizon = :investmentHorizon AND " +
           "c.isActive = true")
    List<Client> findByRiskProfileAndInvestmentHorizon(
            @Param("riskProfile") String riskProfile,
            @Param("investmentHorizon") String investmentHorizon);
            
    List<Client> findByUserId(Long userId);
    
    @Query("SELECT c FROM Client c WHERE " +
           "c.user.id = :userId AND " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.pan) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Client> searchClientsByUser(@Param("userId") Long userId, @Param("searchTerm") String searchTerm);
} 