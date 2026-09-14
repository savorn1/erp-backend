package com.example.erp.entity;

public enum SerialNumberStatus {
    // Reserved by a goods receipt line whose quality check hasn't been
    // recorded yet — counts against the global uniqueness check (see
    // SerialNumberRepository.existsBySerialNumber) but isn't usable stock.
    PENDING_QC,
    IN_STOCK,
    // Set while a stock transfer carrying this unit has been shipped but not
    // yet received at its destination.
    IN_TRANSIT,
    // Terminal for this scope — set when a delivery ships the unit out.
    ISSUED,
    // Terminal — set when an approved StockAdjustment removes this unit
    // (DAMAGED, LOST, EXPIRED, or a manual STOCK_DECREASE).
    ADJUSTED_OUT,
    // Terminal — set when the goods receipt line that reserved this unit
    // fails quality check; it was never added to stock.
    QC_REJECTED
}
