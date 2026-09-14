package com.example.erp.entity;

// STOCK_INCREASE is the only reason that adds quantity — every other reason
// is inherently a decrease (see StockAdjustmentServiceImpl's sign rule).
public enum StockAdjustmentReason {
    STOCK_INCREASE,
    STOCK_DECREASE,
    DAMAGED,
    LOST,
    EXPIRED
}
