package com.example.erp.entity;

public enum PurchaseOrderStatus {
    DRAFT,
    // Submitted for approval — see PurchaseOrderServiceImpl.approvePurchaseOrder.
    SUBMITTED,
    // Approved internally — ready to be sent to the supplier.
    APPROVED,
    // Sent to the supplier — see PurchaseOrderServiceImpl.sendPurchaseOrder.
    // Goods receipts are only allowed once a PO reaches this state.
    SENT,
    PARTIALLY_RECEIVED,
    RECEIVED,
    CANCELLED
}
