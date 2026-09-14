package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Shared shape for the Out of Stock / Overstock / Negative Stock reports —
// see InventoryReportServiceImpl. Each just filters the same per-product/
// warehouse dataset (InventoryOverviewService.getOverview) differently.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockThresholdResponse {

    private List<InventoryOverviewResponse> rows;
    private int count;
}
