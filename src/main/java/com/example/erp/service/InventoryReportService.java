package com.example.erp.service;

import com.example.erp.dto.BatchLotFilterRequest;
import com.example.erp.dto.BatchLotStockResponse;
import com.example.erp.dto.InventoryOverviewFilterRequest;
import com.example.erp.dto.LowStockResponse;
import com.example.erp.dto.PartyStockFilterRequest;
import com.example.erp.dto.StockByPartyResponse;
import com.example.erp.dto.StockAgingResponse;
import com.example.erp.dto.StockDetailResponse;
import com.example.erp.dto.StockLedgerFilterRequest;
import com.example.erp.dto.StockLedgerResponse;
import com.example.erp.dto.StockMovementReportFilterRequest;
import com.example.erp.dto.StockInOutResponse;
import com.example.erp.dto.StockOpeningClosingResponse;
import com.example.erp.dto.StockProfitabilityResponse;
import com.example.erp.dto.StockCountVarianceFilterRequest;
import com.example.erp.dto.StockCountVarianceResponse;
import com.example.erp.dto.StockThresholdResponse;
import com.example.erp.dto.StockTurnoverFilterRequest;
import com.example.erp.dto.StockTurnoverResponse;
import com.example.erp.dto.StockValuationResponse;

public interface InventoryReportService {

    // Reuses InventoryOverviewService's per-product/warehouse data, grouped
    // by warehouse.
    StockValuationResponse stockValuation(InventoryOverviewFilterRequest filter);

    // Rows where Product.reorderPoint > 0 and availableStock is below it.
    LowStockResponse lowStock(InventoryOverviewFilterRequest filter);

    // Rows where currentStock <= 0.
    StockThresholdResponse outOfStock(InventoryOverviewFilterRequest filter);

    // Rows where Product.maxStock > 0 and currentStock exceeds it.
    StockThresholdResponse overstock(InventoryOverviewFilterRequest filter);

    // Rows where currentStock < 0 — a data-integrity report; should normally
    // be empty.
    StockThresholdResponse negativeStock(InventoryOverviewFilterRequest filter);

    // Bin-level current stock — finer grain than the warehouse-summed
    // InventoryOverviewResponse.
    StockDetailResponse stockDetail(InventoryOverviewFilterRequest filter);

    // Chronological StockMovement rows for one product (optionally scoped to
    // one warehouse) with a running balance.
    StockLedgerResponse stockLedger(StockLedgerFilterRequest filter);

    // Inbound movement totals (RECEIPT/TRANSFER_IN/PRODUCTION_OUTPUT) per
    // product/warehouse over a date range.
    StockInOutResponse stockIn(StockMovementReportFilterRequest filter);

    // Outbound movement totals (ISSUE/TRANSFER_OUT/MATERIAL_CONSUMPTION) per
    // product/warehouse over a date range, reported as positive magnitudes.
    StockInOutResponse stockOut(StockMovementReportFilterRequest filter);

    // Quantity on hand as of the start and end of a date range, per
    // product/warehouse, derived by replaying StockMovement deltas.
    StockOpeningClosingResponse openingClosingStock(StockMovementReportFilterRequest filter);

    // Current stock bucketed by days since the most recent inbound movement.
    StockAgingResponse stockAging(InventoryOverviewFilterRequest filter);

    // Outbound movement quantity per product/warehouse over a trailing
    // window — backs Slow Moving, Fast Moving, and Dead Stock (client sorts/
    // filters the same rows differently).
    StockTurnoverResponse stockTurnover(StockTurnoverFilterRequest filter);

    // Per-batch/lot quantity (received - issued - adjusted out + adjusted
    // in) and expiration date — backs both Batch/Lot Stock and Expiry Stock.
    BatchLotStockResponse batchLotStock(BatchLotFilterRequest filter);

    // Quantity on open sales orders per customer — not physically segregated
    // stock, just what's reserved against each customer's orders.
    StockByPartyResponse stockByCustomer(PartyStockFilterRequest filter);

    // Quantity on open purchase orders per supplier — incoming, not yet
    // received.
    StockByPartyResponse stockBySupplier(PartyStockFilterRequest filter);

    // Quantity sold, revenue, COGS, and turnover per product over a date
    // range, using Product.costPrice/sellingPrice as the cost basis — backs
    // both Stock Profitability and Inventory Performance.
    StockProfitabilityResponse stockProfitability(StockMovementReportFilterRequest filter);

    // Every StockCountLine flattened with its parent count's header fields —
    // backs both Inventory Count and Stock Variance.
    StockCountVarianceResponse stockCountVariance(StockCountVarianceFilterRequest filter);
}
