package com.example.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// One new item taken instead, priced at today's product price the same way
// a PosSaleLine is (see PosPricingService) — net/tax/costPrice are kept
// alongside lineTotal so PosExchangeServiceImpl can total and post without
// re-deriving them from productId + quantity later.
@Entity
@Table(name = "pos_exchange_new_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosExchangeNewLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pos_exchange_id", nullable = false)
    private Long posExchangeId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal net;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal tax;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal lineTotal;

    @Column(name = "cost_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal costPrice;

    // Portion of `quantity` that exceeded on-hand and was let through anyway
    // under InventorySettings.backorderEnabled — 0 otherwise. See
    // PosStockService.decrease's return value and StockAvailabilityService.
    @Column(name = "backordered_quantity", nullable = false, precision = 19, scale = 4,
            columnDefinition = "numeric(19,4) not null default 0")
    @Builder.Default
    private BigDecimal backorderedQuantity = BigDecimal.ZERO;
}
