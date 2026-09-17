package com.example.erp.service.impl;

import com.example.erp.entity.InventorySettings;
import com.example.erp.entity.Role;
import com.example.erp.entity.StockLevel;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.service.InventorySettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// The one place InventorySettings is actually enforced — used by POS
// checkout/exchange (via PosStockService.decrease) and Sales Order
// confirmation/delivery. Deliberately NOT used by Stock Transfer, Stock
// Adjustment, or Manufacturing Order consumption — those are internal stock
// movements, not sales, and keep their own unconditional hard blocks.
@Component
@RequiredArgsConstructor
public class StockAvailabilityService {

    private final StockLevelRepository stockLevelRepository;
    private final InventorySettingsService inventorySettingsService;
    private final UserRepository userRepository;

    public BigDecimal onHand(Long productId, Long warehouseId) {
        return stockLevelRepository.findByProductIdAndWarehouseId(productId, warehouseId).stream()
                .map(StockLevel::getQuantityOnHand)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal reserved(Long productId, Long warehouseId) {
        return stockLevelRepository.findByProductIdAndWarehouseIdAndBinIdIsNull(productId, warehouseId)
                .map(StockLevel::getReservedQuantity)
                .orElse(BigDecimal.ZERO);
    }

    // Throws if the requested quantity can't be let through under the
    // company's InventorySettings; otherwise returns the *physical*
    // shortfall (quantity beyond raw on-hand, 0 if none) — the caller uses
    // this to decide how much to record as backordered and how much of the
    // eventual decrement is allowed to push on-hand negative.
    public BigDecimal check(Long companyId, Long productId, Long warehouseId, BigDecimal quantity, String actingUsername) {
        InventorySettings settings = inventorySettingsService.resolveForCompany(companyId);
        BigDecimal onHand = onHand(productId, warehouseId);
        BigDecimal reserved = settings.isReserveStock() ? reserved(productId, warehouseId) : BigDecimal.ZERO;
        BigDecimal available = onHand.subtract(reserved);

        if (quantity.compareTo(available) <= 0) {
            return BigDecimal.ZERO;
        }

        if (!settings.isAllowOverselling() && !settings.isBackorderEnabled()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Insufficient available stock (available " + available + ")");
        }

        if (settings.isAllowOverselling() && settings.isOversellingApprovalRequired() && !isAdmin(actingUsername)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Exceeds available stock (available " + available + ") — requires admin approval");
        }

        BigDecimal physicalShortfall = quantity.subtract(onHand);
        if (physicalShortfall.compareTo(BigDecimal.ZERO) > 0 && !settings.isAllowNegativeStock()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Insufficient stock on hand (" + onHand + ") — negative stock is not allowed");
        }

        return physicalShortfall.compareTo(BigDecimal.ZERO) > 0 ? physicalShortfall : BigDecimal.ZERO;
    }

    private boolean isAdmin(String actingUsername) {
        return userRepository.findByUsername(actingUsername).map(User::getRole).orElse(null) == Role.ADMIN;
    }
}
