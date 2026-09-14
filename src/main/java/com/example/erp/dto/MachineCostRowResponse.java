package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineCostRowResponse {

    private Long machineId;
    private String machineName;
    private long operationCount;
    private BigDecimal totalActualHours;
    // Null when the machine has no costPerHour set.
    private BigDecimal costPerHour;
    private BigDecimal totalCost;
}
