package com.example.erp.entity;

public enum SupplierStatus {
    ACTIVE,
    INACTIVE,
    // On hold — we've stopped placing orders, distinct from a normal INACTIVE.
    BLOCKED
}
