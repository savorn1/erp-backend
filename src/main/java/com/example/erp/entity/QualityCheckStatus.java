package com.example.erp.entity;

// A goods receipt line's inspection outcome. Stock only moves — and the
// PurchaseOrderLine's quantityReceived only advances — once a line reaches
// PASSED (see GoodsReceiptServiceImpl.recordQualityCheck). FAILED quantity is
// never added to stock and is left off the PO line's received total, so the
// remaining quantity stays open for a replacement shipment.
public enum QualityCheckStatus {
    PENDING,
    PASSED,
    FAILED
}
