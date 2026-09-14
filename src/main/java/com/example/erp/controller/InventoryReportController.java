package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.InventoryOverviewFilterRequest;
import com.example.erp.dto.LowStockResponse;
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
@PreAuthorize("hasRole('ADMIN')")
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
}
