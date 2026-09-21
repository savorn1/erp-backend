package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOverviewResponse {

    private Long productId;
    private String productName;
    private String productSku;
    private Long warehouseId;
    private String warehouseName;

    // Sum of StockLevel.quantityOnHand across every bin in this warehouse.
    // The product's inventory unit. Every quantity below is in this unit, and
    // without it the numbers are just bare figures — 40 bottles and 40 pallets
    // look identical in the grid.
    private String unitOfMeasureAbbreviation;

    private BigDecimal currentStock;
    // Quantity on outstanding submitted/partially-delivered sales orders —
    // still physically on hand, but already spoken for.
    private BigDecimal reservedStock;
    // currentStock - reservedStock.
    private BigDecimal availableStock;
    // Expected to arrive: outstanding purchase orders + inbound stock
    // transfers already shipped but not yet received here.
    private BigDecimal incomingStock;
    // Already left: outbound stock transfers shipped from here but not yet
    // received at their destination.
    private BigDecimal outgoingStock;

    private BigDecimal unitCost;
    // currentStock * unitCost.
    private BigDecimal valuationValue;
    // Product.reorderPoint — null/zero means no threshold configured, so
    // this row is never flagged by the Low Stock report.
    private BigDecimal reorderPoint;
    // Product.maxStock — null/zero means no threshold configured, so this
    // row is never flagged by the Overstock report.
    private BigDecimal maxStock;
}
