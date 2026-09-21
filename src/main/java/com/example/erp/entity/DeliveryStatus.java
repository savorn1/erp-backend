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
    CANCELLED;

    /** The authoritative delivery lifecycle; shipping is the stock-moving step. */
    public boolean canTransitionTo(DeliveryStatus next) {
        return switch (this) {
            case PENDING -> next == PICKED || next == CANCELLED;
            case PICKED -> next == PACKED || next == CANCELLED;
            case PACKED -> next == SHIPPED || next == CANCELLED;
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
