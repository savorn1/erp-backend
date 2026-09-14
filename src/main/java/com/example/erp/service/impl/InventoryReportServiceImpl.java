package com.example.erp.service.impl;

import com.example.erp.dto.InventoryOverviewFilterRequest;
import com.example.erp.dto.InventoryOverviewResponse;
import com.example.erp.dto.LowStockResponse;
import com.example.erp.dto.StockValuationResponse;
import com.example.erp.dto.StockValuationRowResponse;
import com.example.erp.service.InventoryOverviewService;
import com.example.erp.service.InventoryReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryReportServiceImpl implements InventoryReportService {

    private final InventoryOverviewService inventoryOverviewService;

    @Override
    @Transactional(readOnly = true)
    public StockValuationResponse stockValuation(InventoryOverviewFilterRequest filter) {
        List<InventoryOverviewResponse> rows = fetchAll(filter);

        Map<Long, StockValuationRowResponse> byWarehouse = new LinkedHashMap<>();
        Map<Long, Integer> productCounts = new LinkedHashMap<>();
        for (InventoryOverviewResponse row : rows) {
            StockValuationRowResponse bucket = byWarehouse.computeIfAbsent(row.getWarehouseId(), id -> StockValuationRowResponse.builder()
                    .warehouseId(row.getWarehouseId())
                    .warehouseName(row.getWarehouseName())
                    .totalQuantity(BigDecimal.ZERO)
                    .totalValue(BigDecimal.ZERO)
                    .build());
            bucket.setTotalQuantity(bucket.getTotalQuantity().add(row.getCurrentStock()));
            bucket.setTotalValue(bucket.getTotalValue().add(row.getValuationValue()));
            productCounts.merge(row.getWarehouseId(), 1, Integer::sum);
        }
        byWarehouse.values().forEach(bucket -> bucket.setProductCount(productCounts.getOrDefault(bucket.getWarehouseId(), 0)));

        List<StockValuationRowResponse> sortedRows = new ArrayList<>(byWarehouse.values());
        sortedRows.sort(Comparator.comparing(StockValuationRowResponse::getTotalValue).reversed());

        BigDecimal totalQuantity = sortedRows.stream().map(StockValuationRowResponse::getTotalQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalValue = sortedRows.stream().map(StockValuationRowResponse::getTotalValue).reduce(BigDecimal.ZERO, BigDecimal::add);

        return StockValuationResponse.builder()
                .rows(sortedRows)
                .totalQuantity(totalQuantity)
                .totalValue(totalValue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LowStockResponse lowStock(InventoryOverviewFilterRequest filter) {
        List<InventoryOverviewResponse> rows = fetchAll(filter).stream()
                .filter(row -> row.getReorderPoint() != null && row.getReorderPoint().compareTo(BigDecimal.ZERO) > 0)
                .filter(row -> row.getAvailableStock().compareTo(row.getReorderPoint()) < 0)
                .sorted(Comparator.comparing((InventoryOverviewResponse row) -> row.getAvailableStock().subtract(row.getReorderPoint())))
                .toList();

        return LowStockResponse.builder()
                .rows(rows)
                .count(rows.size())
                .build();
    }

    private List<InventoryOverviewResponse> fetchAll(InventoryOverviewFilterRequest filter) {
        filter.setSize(1_000_000);
        filter.setPage(1);
        return inventoryOverviewService.getOverview(filter).getData();
    }
}
