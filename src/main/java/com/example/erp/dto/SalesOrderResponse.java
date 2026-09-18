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
public class SalesOrderResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long customerId;
    private String customerName;
    // Set when this order was converted from an accepted Quotation.
    private Long quotationId;
    private Long warehouseId;
    private String warehouseName;
    private String soNumber;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private String status;
    private String notes;
    private String createdBy;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String foreignCurrency;
    private BigDecimal exchangeRate;
    // totalAmount / exchangeRate, only when both foreignCurrency and
    // exchangeRate are set — a display-only convenience, never stored.
    private BigDecimal foreignTotalAmount;
    // Only populated while status == SUBMITTED — see
    // ApprovalWorkflowService. Null once the order has moved past that
    // status (or if no ApprovalRule ever applied).
    private Integer approvalsRequired;
    private Integer approvalsRecorded;
    private List<SalesOrderLineResponse> lines;
}
