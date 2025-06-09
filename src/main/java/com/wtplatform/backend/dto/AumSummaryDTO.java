package com.wtplatform.backend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AumSummaryDTO {
    private double totalAum;
    private int contributingClients;
    private LocalDateTime lastUpdated;

    public double getTotalAum() {
        return totalAum;
    }

    public void setTotalAum(double totalAum) {
        this.totalAum = totalAum;
    }

    public int getContributingClients() {
        return contributingClients;
    }

    public void setContributingClients(int contributingClients) {
        this.contributingClients = contributingClients;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}