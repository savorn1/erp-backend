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
    CANCELLED
}
