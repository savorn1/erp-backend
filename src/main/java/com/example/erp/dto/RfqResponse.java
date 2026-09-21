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
public class RfqResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long warehouseId;
    private String warehouseName;
    private Long purchaseRequestId;
    private String purchaseRequestNumber;
    private String rfqNumber;
    private LocalDate issueDate;
    private String status;

    // Where the workflow actually got to, for documents that were
    // cancelled or rejected. Null when unknown — including every row
    // cancelled before this was recorded.
    private String cancelledFromStatus;
    private String notes;
    private String createdBy;
    private Long awardedSupplierId;
    private String awardedSupplierName;
    private Long awardedPurchaseOrderId;
    private String awardedPurchaseOrderNumber;
    private List<RfqLineResponse> lines;
    private List<RfqSupplierResponse> suppliers;
}
