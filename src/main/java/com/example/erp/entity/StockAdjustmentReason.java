package com.example.erp.entity;

// STOCK_INCREASE and OPENING_BALANCE are the only reasons that add quantity
// — every other reason is inherently a decrease (see
// StockAdjustmentServiceImpl's sign rule). OPENING_BALANCE is functionally
// identical to STOCK_INCREASE; it exists as a distinct label so a product's
// or warehouse's initial stock entry stays visually and semantically
// separate from a later correction in the adjustment history.
public enum StockAdjustmentReason {
    STOCK_INCREASE,
    OPENING_BALANCE,
    STOCK_DECREASE,
    DAMAGED,
    LOST,
    EXPIRED
}
