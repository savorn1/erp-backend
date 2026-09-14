package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePendingGoodsReceiptsResponse {

    // Receipts still PENDING_QC — sorted by receipt date, ascending.
    private List<PurchasePendingGoodsReceiptRowResponse> rows;
    private long receiptCount;
}
