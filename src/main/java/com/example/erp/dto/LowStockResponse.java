package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Every product/warehouse row where reorderPoint > 0 and availableStock is
// below it — see InventoryReportServiceImpl.lowStock. Sorted with the most
// critical shortfall first.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockResponse {

    private List<InventoryOverviewResponse> rows;
    private int count;
}
