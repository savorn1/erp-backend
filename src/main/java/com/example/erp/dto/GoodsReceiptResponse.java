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
public class GoodsReceiptResponse {

    private Long id;
    private Long companyId;
    private Long purchaseOrderId;
    private String poNumber;
    private Long warehouseId;
    private String warehouseName;
    private String receiptNumber;
    private LocalDate receiptDate;
    // PENDING_QC / COMPLETED — see GoodsReceiptStatus.
    private String status;
    private String notes;
    private String createdBy;
    private List<GoodsReceiptLineResponse> lines;
}
