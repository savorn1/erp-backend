package com.example.erp.entity;

public enum GoodsReceiptStatus {
    // At least one line is still awaiting a quality check result.
    PENDING_QC,
    // Every line has been passed or failed — see GoodsReceiptServiceImpl.recordQualityCheck.
    COMPLETED
}
