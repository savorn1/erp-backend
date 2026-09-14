package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManufacturingOrderResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long bomId;
    private String bomNumber;
    private Long productId;
    private String productName;
    private String productSku;
    private Long warehouseId;
    private String warehouseName;
    private Long productionPlanId;
    private String planNumber;
    private String moNumber;
    private BigDecimal plannedQuantity;
    private BigDecimal producedQuantity;
    private BigDecimal scrapQuantity;
    private String scrapReason;
    private String status;
    private String qualityStatus;
    private String qualityNotes;
    private String qualityCheckedBy;
    private LocalDateTime qualityCheckedAt;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDateTime actualStartDate;
    private LocalDateTime actualEndDate;
    private BigDecimal materialCost;
    private BigDecimal laborCost;
    private BigDecimal overheadCost;
    private BigDecimal totalCost;
    private BigDecimal unitCost;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<ManufacturingOrderMaterialResponse> materials;
    // Empty when the BOM has no active routing — see WorkOrder's own comment.
    private List<WorkOrderResponse> workOrders;
}
