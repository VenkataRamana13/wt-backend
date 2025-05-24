package com.wtplatform.backend.repository;

import com.wtplatform.backend.model.NavHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface NavHistoryRepository extends JpaRepository<NavHistory, Long> {
    
    Optional<NavHistory> findByFundIdAndNavDate(Long fundId, LocalDate navDate);
    
    @Query("SELECT n FROM NavHistory n WHERE n.fundId = :fundId AND n.navDate <= :date ORDER BY n.navDate DESC LIMIT 1")
    Optional<NavHistory> findLatestNavBeforeDate(@Param("fundId") Long fundId, @Param("date") LocalDate date);
} 