package com.example.erp.entity;

// Determines what a goods receipt must capture when receiving this product —
// see GoodsReceiptServiceImpl's up-front validation.
public enum ProductTrackingType {
    NONE,
    BATCH,
    SERIAL
}
