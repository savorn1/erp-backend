package com.example.erp.entity;

public enum InvoiceStatus {
    DRAFT,
    APPROVED,
    CANCELLED;

    public boolean canTransitionTo(InvoiceStatus next) {
        return (this == DRAFT && (next == APPROVED || next == CANCELLED)) || (this == APPROVED && next == CANCELLED);
    }
}
