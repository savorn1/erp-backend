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

@Entity
@Table(name = "sales_order_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sales_order_id", nullable = false)
    private Long salesOrderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    // The unit quantityOrdered/quantityDelivered are expressed in — e.g. selling
    // "10 BOX" of a product whose inventory unit is PCS. Nullable — added to an
    // already-populated table; a null read means "the product's own base unit,
    // factor 1" (see SalesOrderServiceImpl.resolveLineUnit).
    @Column(name = "unit_of_measure_id")
    private Long unitOfMeasureId;

    // Snapshotted from ProductUom.conversionFactor at order time — how many of
    // the product's base/inventory unit equal 1 of unitOfMeasureId above. Frozen
    // here (not re-looked-up) so editing that ProductUom afterwards can't
    // silently change what an already-placed order meant, and so deliveries
    // against it keep converting to stock the same way.
    @Column(name = "conversion_factor", precision = 19, scale = 6)
    private BigDecimal conversionFactor;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityOrdered;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    // Percent, e.g. 10.00 = 10% — defaults to 0 when not supplied.
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    // Percent — defaults from Product.taxRate at line creation when not
    // supplied explicitly (see SalesOrderServiceImpl.saveLines).
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    // Cumulative quantity delivered across all deliveries against this line —
    // updated by DeliveryServiceImpl, never edited directly.
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal quantityDelivered = BigDecimal.ZERO;

    // Portion of quantityOrdered that exceeded on-hand at confirmation and
    // was let through anyway under InventorySettings.backorderEnabled — 0
    // otherwise. Set once at approveSalesOrder, never adjusted afterward
    // (quantityOrdered itself is locked post-confirmation). columnDefinition
    // carries an explicit SQL default — see StockLevel.reservedQuantity for
    // why (this table already has rows).
    @Column(name = "backordered_quantity", nullable = false, precision = 19, scale = 4,
            columnDefinition = "numeric(19,4) not null default 0")
    @Builder.Default
    private BigDecimal backorderedQuantity = BigDecimal.ZERO;
}
