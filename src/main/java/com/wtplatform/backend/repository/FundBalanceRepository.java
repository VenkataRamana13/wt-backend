package com.wtplatform.backend.repository;

import com.wtplatform.backend.model.FundBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.math.BigDecimal;

@Repository
public interface FundBalanceRepository extends JpaRepository<FundBalance, Long> {
    
    Optional<FundBalance> findByFundIdAndClientId(String fundId, Long clientId);
    
    List<FundBalance> findByClientId(Long clientId);
    
    @Query("SELECT fb FROM FundBalance fb WHERE fb.fundId = :fundId AND fb.client.id = :clientId AND fb.balance >= :requiredAmount")
    Optional<FundBalance> findSufficientBalance(
        @Param("fundId") String fundId, 
        @Param("clientId") Long clientId, 
        @Param("requiredAmount") BigDecimal requiredAmount
    );
} 