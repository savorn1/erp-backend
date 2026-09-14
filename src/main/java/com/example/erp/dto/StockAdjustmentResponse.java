package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentResponse {

    private Long id;
    private Long companyId;
    private Long warehouseId;
    private String warehouseName;
    private String adjustmentNumber;
    private LocalDate adjustmentDate;
    private String status;
    private String notes;
    private String requestedBy;
    private String approvedBy;
    private LocalDate approvalDate;
    private List<StockAdjustmentLineResponse> lines;
}
