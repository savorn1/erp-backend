package com.example.erp.entity;

public enum ManufacturingOrderStatus {
    DRAFT,
    RELEASED,
    // Materials have already been drawn from stock — see
    // ManufacturingOrderServiceImpl.startProduction. Cancelling is no longer
    // allowed once an order reaches this state (no reversal flow exists).
    IN_PROGRESS,
    // Production has finished (producedQuantity/scrapQuantity/cost are
    // stamped) but the output hasn't cleared quality control yet — finished
    // goods are not yet in stock. See
    // ManufacturingOrderServiceImpl.recordQualityCheck.
    PENDING_QC,
    COMPLETED,
    CANCELLED
}
