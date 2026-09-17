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

// taxRate is a raw percent snapshot copied from Product.taxRate at
// add-to-cart time — same convention as SalesOrderLine/InvoiceLine, not a
// TaxRate FK (see TaxRate.java's own comment on why lines don't reference it).
@Entity
@Table(name = "pos_sale_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosSaleLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pos_sale_id", nullable = false)
    private Long posSaleId;

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

    @Column(name = "line_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal lineTotal;

    // Running total returned against this line across every PosExchange that
    // has touched it — a return request is rejected once quantity minus this
    // can't cover the requested amount (see PosExchangeServiceImpl).
    // columnDefinition carries an explicit SQL default: added after this
    // table already had rows, and ddl-auto=update's "add column ... not
    // null" (with no default) fails against Postgres once any row exists.
    @Column(name = "returned_quantity", nullable = false, precision = 19, scale = 4,
            columnDefinition = "numeric(19,4) not null default 0")
    @Builder.Default
    private BigDecimal returnedQuantity = BigDecimal.ZERO;

    // Portion of `quantity` that exceeded on-hand at checkout and was let
    // through anyway under InventorySettings.backorderEnabled — 0 otherwise.
    // See PosStockService.decrease's return value and StockAvailabilityService.
    @Column(name = "backordered_quantity", nullable = false, precision = 19, scale = 4,
            columnDefinition = "numeric(19,4) not null default 0")
    @Builder.Default
    private BigDecimal backorderedQuantity = BigDecimal.ZERO;
}
