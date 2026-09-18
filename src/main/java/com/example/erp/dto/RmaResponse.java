package com.example.erp.dto;

import com.example.erp.entity.RmaResolutionType;
import com.example.erp.entity.RmaStatus;
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
public class RmaResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long customerId;
    private String customerName;
    private Long invoiceId;
    private String invoiceNumber;
    private Long warehouseId;
    private String warehouseName;
    private String rmaNumber;
    private LocalDate requestDate;
    private RmaStatus status;
    private RmaResolutionType resolutionType;
    private String reason;
    private String notes;
    private String createdBy;
    // Sum of quantity * unitPrice across every line — informational, what a
    // REFUND resolution would credit.
    private BigDecimal totalRefundable;
    private List<RmaLineResponse> lines;
}
