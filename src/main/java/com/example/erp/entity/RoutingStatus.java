package com.example.erp.entity;

public enum RoutingStatus {
    ACTIVE,
    // Kept for history, but ManufacturingOrderServiceImpl only picks up an
    // ACTIVE routing when generating a new order's WorkOrders.
    INACTIVE
}
