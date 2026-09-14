package com.example.erp.service;

import com.example.erp.dto.CreateGoodsReceiptRequest;
import com.example.erp.dto.GoodsReceiptFilterRequest;
import com.example.erp.dto.GoodsReceiptResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.QualityCheckRequest;

public interface GoodsReceiptService {

    PageResponse<GoodsReceiptResponse> listGoodsReceipts(GoodsReceiptFilterRequest filter);

    GoodsReceiptResponse getGoodsReceipt(Long id);

    // Posts a receipt against a purchase order: validates quantities against
    // what's still outstanding on each PO line (net of other receipts still
    // pending quality check) and records each line PENDING_QC. Stock does
    // NOT move yet — see recordQualityCheck.
    GoodsReceiptResponse createGoodsReceipt(CreateGoodsReceiptRequest request, String actingUsername);

    // Records a line's inspection result. PASSED increases stock (StockLevel),
    // records the movement (StockMovement), advances the PO line's
    // quantityReceived, and re-evaluates the PO's status. FAILED leaves all of
    // that untouched, so the quantity stays open for a replacement shipment.
    GoodsReceiptResponse recordQualityCheck(Long receiptId, Long lineId, QualityCheckRequest request, String actingUsername);
}
