package com.wtplatform.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "amfi_schemes")
public class AmfiScheme {
    
    @Id
    private String schemeCode;
    
    private String schemeName;
    private String amcName;
    private String schemeType;
    private String isin;
    private String category;
    private Boolean isActive;
    
    @Column(nullable = true)
    private BigDecimal lastNavValue;
    
    @Column(nullable = true)
    private LocalDate lastNavDate;

    public String getSchemeCode() {
        return schemeCode;
    }

    public void setSchemeCode(String schemeCode) {
        this.schemeCode = schemeCode;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public String getAmcName() {
        return amcName;
    }

    public void setAmcName(String amcName) {
        this.amcName = amcName;
    }

    public String getSchemeType() {
        return schemeType;
    }

    public void setSchemeType(String schemeType) {
        this.schemeType = schemeType;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public BigDecimal getLastNavValue() {
        return lastNavValue;
    }

    public void setLastNavValue(BigDecimal lastNavValue) {
        this.lastNavValue = lastNavValue;
    }

    public LocalDate getLastNavDate() {
        return lastNavDate;
    }

    public void setLastNavDate(LocalDate lastNavDate) {
        this.lastNavDate = lastNavDate;
    }
} 