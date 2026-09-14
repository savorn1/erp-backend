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
public class MachineUtilizationRowResponse {

    private Long machineId;
    private String machineName;
    private String machineStatus;
    private long operationCount;
    private BigDecimal totalActualHours;
    private BigDecimal averageHoursPerOperation;
}
