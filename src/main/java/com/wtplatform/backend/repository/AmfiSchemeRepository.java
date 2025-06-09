package com.wtplatform.backend.repository;

import com.wtplatform.backend.model.AmfiScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface AmfiSchemeRepository extends JpaRepository<AmfiScheme, String> {
    // Basic CRUD operations are automatically provided by JpaRepository

    @Modifying
    @Transactional
    @Query("UPDATE AmfiScheme s SET s.lastNavValue = :nav, s.lastNavDate = :date WHERE s.schemeCode = :code")
    void updateNav(@Param("code") String schemeCode, @Param("nav") BigDecimal nav, @Param("date") LocalDate date);
} 