package com.example.erp.service;

import com.example.erp.dto.InventoryOverviewFilterRequest;
import com.example.erp.dto.LowStockResponse;
import com.example.erp.dto.StockValuationResponse;

public interface InventoryReportService {

    // Reuses InventoryOverviewService's per-product/warehouse data, grouped
    // by warehouse.
    StockValuationResponse stockValuation(InventoryOverviewFilterRequest filter);

    // Rows where Product.reorderPoint > 0 and availableStock is below it.
    LowStockResponse lowStock(InventoryOverviewFilterRequest filter);
}
