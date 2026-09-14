package com.example.erp.entity;

public enum BillOfMaterialStatus {
    ACTIVE,
    // Kept for history, but ManufacturingOrderServiceImpl refuses to create
    // new orders against an inactive BOM.
    INACTIVE
}
