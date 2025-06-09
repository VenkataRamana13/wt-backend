package com.wtplatform.backend.repository;

import com.wtplatform.backend.model.NavHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NavHistoryRepository extends JpaRepository<NavHistory, Long> {
    
    Optional<NavHistory> findByFundIdAndNavDate(Long fundId, LocalDate navDate);
    
    @Query("SELECT n FROM NavHistory n WHERE n.fundId = :fundId AND n.navDate <= :date ORDER BY n.navDate DESC LIMIT 1")
    Optional<NavHistory> findLatestNavBeforeDate(@Param("fundId") Long fundId, @Param("date") LocalDate date);

    @Query("SELECT n.fundId, n.nav, n.navDate, n.source FROM NavHistory n WHERE n.navDate = (SELECT MAX(n2.navDate) FROM NavHistory n2 WHERE n2.fundId = n.fundId)")
    List<Object[]> findLatestNavForAllSchemes();
} 