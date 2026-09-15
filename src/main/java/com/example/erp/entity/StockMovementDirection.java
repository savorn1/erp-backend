package com.example.erp.entity;

// Not a stored column — derived from StockMovement.quantityDelta's sign
// (>= 0 is IN, < 0 is OUT). Exists as a query/response-only concept because
// ADJUSTMENT movements can go either way, so direction can't be inferred
// from StockMovementType alone (unlike RECEIPT/ISSUE/etc.).
public enum StockMovementDirection {
    IN,
    OUT
}
