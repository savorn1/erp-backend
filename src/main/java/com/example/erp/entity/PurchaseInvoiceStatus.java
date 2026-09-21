package com.example.erp.entity;

public enum PurchaseInvoiceStatus {
    DRAFT,
    APPROVED,
    CANCELLED;

    public boolean canTransitionTo(PurchaseInvoiceStatus next) {
        return (this == DRAFT && (next == APPROVED || next == CANCELLED)) || (this == APPROVED && next == CANCELLED);
    }
}
