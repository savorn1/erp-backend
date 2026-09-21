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
    CANCELLED;

    /** The authoritative purchase-order lifecycle. */
    public boolean canTransitionTo(PurchaseOrderStatus next) {
        return switch (this) {
            case DRAFT -> next == SUBMITTED || next == CANCELLED;
            case SUBMITTED -> next == APPROVED || next == CANCELLED;
            case APPROVED -> next == SENT || next == CANCELLED;
            case SENT -> next == PARTIALLY_RECEIVED || next == RECEIVED || next == CANCELLED;
            case PARTIALLY_RECEIVED -> next == RECEIVED;
            case RECEIVED, CANCELLED -> false;
        };
    }
}
