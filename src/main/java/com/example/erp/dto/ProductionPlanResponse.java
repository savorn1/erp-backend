package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionPlanResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private String planNumber;
    private String name;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String status;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
    // Aggregated from the ManufacturingOrders linked to this plan.
    private long orderCount;
    private BigDecimal totalPlannedQuantity;
    private BigDecimal totalProducedQuantity;
}
