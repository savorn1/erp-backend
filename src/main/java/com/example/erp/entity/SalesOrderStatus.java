package com.example.erp.entity;

public enum SalesOrderStatus {
    DRAFT,
    // Submitted for approval — see SalesOrderServiceImpl.approveSalesOrder.
    SUBMITTED,
    // Approved: stock availability confirmed, order confirmed to the
    // customer, and now eligible for delivery.
    CONFIRMED,
    PARTIALLY_DELIVERED,
    DELIVERED,
    CANCELLED;

    /** The authoritative sales-order lifecycle, used by services that change status. */
    public boolean canTransitionTo(SalesOrderStatus next) {
        return switch (this) {
            case DRAFT -> next == SUBMITTED || next == CANCELLED;
            case SUBMITTED -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == PARTIALLY_DELIVERED || next == DELIVERED || next == CANCELLED;
            case PARTIALLY_DELIVERED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
