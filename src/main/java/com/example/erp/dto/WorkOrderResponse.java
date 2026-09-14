package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderResponse {

    private Long id;
    private Long manufacturingOrderId;
    private String moNumber;
    private Integer sequenceNumber;
    private String name;
    private Long workCenterId;
    private String workCenterName;
    private Long machineId;
    private String machineName;
    private BigDecimal standardTimeMinutes;
    private String status;
    private LocalDateTime actualStartDate;
    private LocalDateTime actualEndDate;
    private String notes;
}
