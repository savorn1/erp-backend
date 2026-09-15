package com.example.erp.service.impl;

import com.example.erp.entity.StockLevel;
import com.example.erp.entity.StockMovement;
import com.example.erp.entity.StockMovementType;
import com.example.erp.exception.AppException;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

// Shared stock-movement helpers for every POS flow that moves stock —
// checkout, void, and POS exchange (both its return and new-item sides).
// decrease() drains largest-bin-first under a pessimistic lock, since POS
// checkout/exchange is the one place two registers can race on the same
// product/warehouse row. increase() always restores onto the unbinned row
// rather than recovering exact bin provenance — same simplification every
// other increase-side helper in this codebase already makes.
@Component
@RequiredArgsConstructor
public class PosStockService {

    private final StockLevelRepository stockLevelRepository;
    private final StockMovementRepository stockMovementRepository;

    public void decrease(Long companyId, Long warehouseId, Long productId, BigDecimal quantity,
                          String referenceType, Long referenceId, String actingUsername) {
        List<StockLevel> stockLevels = stockLevelRepository.findByProductIdAndWarehouseIdForUpdate(productId, warehouseId).stream()
                .sorted(Comparator.comparing(StockLevel::getQuantityOnHand).reversed())
                .toList();
        BigDecimal remaining = quantity;
        for (StockLevel stockLevel : stockLevels) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal take = stockLevel.getQuantityOnHand().min(remaining);
            stockLevel.setQuantityOnHand(stockLevel.getQuantityOnHand().subtract(take));
            stockLevelRepository.save(stockLevel);
            remaining = remaining.subtract(take);
        }
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Insufficient stock for product id " + productId);
        }
        stockMovementRepository.save(StockMovement.builder()
                .companyId(companyId)
                .productId(productId)
                .warehouseId(warehouseId)
                .type(StockMovementType.ISSUE)
                .quantityDelta(quantity.negate())
                .referenceType(referenceType)
                .referenceId(referenceId)
                .createdBy(actingUsername)
                .build());
    }

    public void increase(Long companyId, Long warehouseId, Long productId, BigDecimal quantity,
                          String referenceType, Long referenceId, String actingUsername) {
        StockLevel stockLevel = stockLevelRepository.findByProductIdAndWarehouseIdAndBinIdIsNull(productId, warehouseId)
                .orElseGet(() -> StockLevel.builder()
                        .companyId(companyId)
                        .productId(productId)
                        .warehouseId(warehouseId)
                        .build());
        stockLevel.setQuantityOnHand(stockLevel.getQuantityOnHand().add(quantity));
        stockLevelRepository.save(stockLevel);

        stockMovementRepository.save(StockMovement.builder()
                .companyId(companyId)
                .productId(productId)
                .warehouseId(warehouseId)
                .type(StockMovementType.ISSUE)
                .quantityDelta(quantity)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .createdBy(actingUsername)
                .build());
    }
}
