package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long supplierId;
    private String supplierName;
    private Long warehouseId;
    private String warehouseName;
    private String poNumber;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private String status;
    private String notes;
    private String createdBy;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    // Only populated while status == SUBMITTED — see ApprovalWorkflowService.
    private Integer approvalsRequired;
    private Integer approvalsRecorded;
    private List<PurchaseOrderLineResponse> lines;
}
