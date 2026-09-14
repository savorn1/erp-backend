package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePendingGoodsReceiptRowResponse {

    private Long receiptId;
    private String receiptNumber;
    private LocalDate receiptDate;
    private Long purchaseOrderId;
    private String poNumber;
    private Long supplierId;
    private String supplierName;
    private Long warehouseId;
    private String warehouseName;
    private long lineCount;
}
