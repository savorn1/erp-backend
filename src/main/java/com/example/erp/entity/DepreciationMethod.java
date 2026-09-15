package com.example.erp.entity;

// Which formula FixedAssetServiceImpl uses to compute a period's
// depreciation amount for an asset.
public enum DepreciationMethod {
    STRAIGHT_LINE,
    DECLINING_BALANCE
}
