package com.wtplatform.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StpTrendDTO {
    private String month; // e.g., "Jan"
    private double amount; // total STP amount for the month
} 