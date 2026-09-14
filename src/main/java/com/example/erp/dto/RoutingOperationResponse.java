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
public class RoutingOperationResponse {

    private Long id;
    private Integer sequenceNumber;
    private String name;
    private Long workCenterId;
    private String workCenterName;
    private Long machineId;
    private String machineName;
    private BigDecimal standardTimeMinutes;
}
