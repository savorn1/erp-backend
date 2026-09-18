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
public class GoodsReceiptLineResponse {

    private Long id;
    private Long purchaseOrderLineId;
    private Long productId;
    private String productName;
    private String productSku;
    private Long unitOfMeasureId;
    private String unitOfMeasureAbbreviation;
    private BigDecimal conversionFactor;
    private BigDecimal quantityReceived;
    // quantityReceived converted to the product's base/inventory unit —
    // what actually landed in stock once this line passed quality check.
    private BigDecimal baseQuantityReceived;
    // Snapshot of the PO line's cost at receipt time — null on receipts
    // posted before landed cost allocation existed.
    private BigDecimal unitCost;
    private Long binId;
    private String binName;
    private Long batchId;
    private String batchNumber;
    private LocalDate expirationDate;
    private List<String> serialNumbers;
    // PENDING / PASSED / FAILED — stock only reflects PASSED lines.
    private String qualityStatus;
    private String qualityNotes;
    private String qualityCheckedBy;
    private LocalDateTime qualityCheckedAt;
}
