package com.wtplatform.backend.dto;

import java.util.Map;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class AumBreakdownDTO {
    private Map<String, Double> byClient;        // Client Name -> AUM Value
    private Map<String, Double> byAssetClass;    // Asset Class -> AUM Value
    private Map<String, Double> byTimeSegment;   // Time Period -> AUM Value

    public Map<String, Double> getByClient() {
        return byClient;
    }

    public void setByClient(Map<String, Double> byClient) {
        this.byClient = byClient;
    }

    public Map<String, Double> getByAssetClass() {
        return byAssetClass;
    }

    public void setByAssetClass(Map<String, Double> byAssetClass) {
        this.byAssetClass = byAssetClass;
    }

    public Map<String, Double> getByTimeSegment() {
        return byTimeSegment;
    }

    public void setByTimeSegment(Map<String, Double> byTimeSegment) {
        this.byTimeSegment = byTimeSegment;
    }
} 