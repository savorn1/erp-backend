package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.BatchLotFilterRequest;
import com.example.erp.dto.BatchLotStockResponse;
import com.example.erp.dto.InventoryOverviewFilterRequest;
import com.example.erp.dto.LowStockResponse;
import com.example.erp.dto.PartyStockFilterRequest;
import com.example.erp.dto.StockAgingResponse;
import com.example.erp.dto.StockByPartyResponse;
import com.example.erp.dto.StockDetailResponse;
import com.example.erp.dto.StockInOutResponse;
import com.example.erp.dto.StockLedgerFilterRequest;
import com.example.erp.dto.StockLedgerResponse;
import com.example.erp.dto.StockCountVarianceFilterRequest;
import com.example.erp.dto.StockCountVarianceResponse;
import com.example.erp.dto.StockMovementReportFilterRequest;
import com.example.erp.dto.StockOpeningClosingResponse;
import com.example.erp.dto.StockProfitabilityResponse;
import com.example.erp.dto.StockThresholdResponse;
import com.example.erp.dto.StockTurnoverFilterRequest;
import com.example.erp.dto.StockTurnoverResponse;
import com.example.erp.dto.StockValuationResponse;
import com.example.erp.service.InventoryReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inventory-reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class InventoryReportController {

    private final InventoryReportService inventoryReportService;

    @GetMapping("/stock-valuation")
    public ResponseEntity<ApiResponse<StockValuationResponse>> stockValuation(@ModelAttribute InventoryOverviewFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.stockValuation(filter)));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<LowStockResponse>> lowStock(@ModelAttribute InventoryOverviewFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.lowStock(filter)));
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<ApiResponse<StockThresholdResponse>> outOfStock(@ModelAttribute InventoryOverviewFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.outOfStock(filter)));
    }

    @GetMapping("/overstock")
    public ResponseEntity<ApiResponse<StockThresholdResponse>> overstock(@ModelAttribute InventoryOverviewFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.overstock(filter)));
    }

    @GetMapping("/negative-stock")
    public ResponseEntity<ApiResponse<StockThresholdResponse>> negativeStock(@ModelAttribute InventoryOverviewFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.negativeStock(filter)));
    }

    @GetMapping("/stock-detail")
    public ResponseEntity<ApiResponse<StockDetailResponse>> stockDetail(@ModelAttribute InventoryOverviewFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.stockDetail(filter)));
    }

    @GetMapping("/stock-ledger")
    public ResponseEntity<ApiResponse<StockLedgerResponse>> stockLedger(@ModelAttribute StockLedgerFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.stockLedger(filter)));
    }

    @GetMapping("/stock-in")
    public ResponseEntity<ApiResponse<StockInOutResponse>> stockIn(@ModelAttribute StockMovementReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.stockIn(filter)));
    }

    @GetMapping("/stock-out")
    public ResponseEntity<ApiResponse<StockInOutResponse>> stockOut(@ModelAttribute StockMovementReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.stockOut(filter)));
    }

    @GetMapping("/opening-closing-stock")
    public ResponseEntity<ApiResponse<StockOpeningClosingResponse>> openingClosingStock(@ModelAttribute StockMovementReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.openingClosingStock(filter)));
    }

    @GetMapping("/stock-aging")
    public ResponseEntity<ApiResponse<StockAgingResponse>> stockAging(@ModelAttribute InventoryOverviewFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.stockAging(filter)));
    }

    @GetMapping("/stock-turnover")
    public ResponseEntity<ApiResponse<StockTurnoverResponse>> stockTurnover(@ModelAttribute StockTurnoverFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.stockTurnover(filter)));
    }

    @GetMapping("/batch-lot-stock")
    public ResponseEntity<ApiResponse<BatchLotStockResponse>> batchLotStock(@ModelAttribute BatchLotFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.batchLotStock(filter)));
    }

    @GetMapping("/stock-by-customer")
    public ResponseEntity<ApiResponse<StockByPartyResponse>> stockByCustomer(@ModelAttribute PartyStockFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.stockByCustomer(filter)));
    }

    @GetMapping("/stock-by-supplier")
    public ResponseEntity<ApiResponse<StockByPartyResponse>> stockBySupplier(@ModelAttribute PartyStockFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.stockBySupplier(filter)));
    }

    @GetMapping("/stock-profitability")
    public ResponseEntity<ApiResponse<StockProfitabilityResponse>> stockProfitability(@ModelAttribute StockMovementReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.stockProfitability(filter)));
    }

    @GetMapping("/stock-count-variance")
    public ResponseEntity<ApiResponse<StockCountVarianceResponse>> stockCountVariance(@ModelAttribute StockCountVarianceFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(inventoryReportService.stockCountVariance(filter)));
    }
}
