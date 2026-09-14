package com.example.erp.entity;

public enum StockMovementType {
    RECEIPT,
    ISSUE,
    TRANSFER_OUT,
    TRANSFER_IN,
    // Manual correction posted by an approved StockAdjustment — signed by the
    // adjustment's reason (positive for STOCK_INCREASE, negative otherwise).
    ADJUSTMENT,
    // Raw material drawn from stock when a ManufacturingOrder starts
    // production — see ManufacturingOrderServiceImpl.startProduction.
    // Always negative.
    MATERIAL_CONSUMPTION,
    // Finished goods added to stock when a ManufacturingOrder completes
    // production — see ManufacturingOrderServiceImpl.completeProduction.
    // Always positive; scrapped units are never posted (they never entered
    // stock in the first place).
    PRODUCTION_OUTPUT
}
