package com.wtplatform.backend.dto;

import java.time.LocalDate;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class AumTrendDTO {
    private LocalDate date;
    private double aumValue;
    private double changePercentage;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getAumValue() {
        return aumValue;
    }

    public void setAumValue(double aumValue) {
        this.aumValue = aumValue;
    }

    public double getChangePercentage() {
        return changePercentage;
    }

    public void setChangePercentage(double changePercentage) {
        this.changePercentage = changePercentage;
    }
} 