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
public class StockTransferResponse {

    private Long id;
    private Long companyId;
    private Long sourceWarehouseId;
    private String sourceWarehouseName;
    private Long destinationWarehouseId;
    private String destinationWarehouseName;
    private String transferNumber;
    private LocalDate requestDate;
    private LocalDate shipDate;
    private LocalDate receiveDate;
    private String status;

    // Where the workflow actually got to, for documents that were
    // cancelled or rejected. Null when unknown — including every row
    // cancelled before this was recorded.
    private String cancelledFromStatus;
    private String notes;
    private String requestedBy;
    private String approvedBy;
    private String shippedBy;
    private String receivedBy;
    private List<StockTransferLineResponse> lines;
}
