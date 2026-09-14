package com.example.erp.entity;

// PENDING -> PICKED -> PACKED -> SHIPPED -> DELIVERED, with CANCELLED
// reachable any time before SHIPPED (stock only moves at ship — see
// DeliveryServiceImpl.shipDelivery).
public enum DeliveryStatus {
    PENDING,
    PICKED,
    PACKED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
