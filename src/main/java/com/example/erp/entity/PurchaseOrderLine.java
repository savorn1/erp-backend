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
@Table(name = "purchase_order_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_order_id", nullable = false)
    private Long purchaseOrderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    // The unit quantityOrdered/quantityReceived are expressed in — e.g.
    // ordering "10 BOX" of a product whose inventory unit is PCS. Nullable —
    // added to an already-populated table; a null read means "the product's
    // own base unit, factor 1" (see PurchaseOrderServiceImpl.resolveLineUnit).
    @Column(name = "unit_of_measure_id")
    private Long unitOfMeasureId;

    // Snapshotted from ProductUom.conversionFactor at order time — how many
    // of the product's base/inventory unit equal 1 of unitOfMeasureId above.
    // Frozen here (not re-looked-up later) so editing that ProductUom row
    // afterward can't silently change what an already-placed order meant.
    @Column(name = "conversion_factor", precision = 19, scale = 6)
    private BigDecimal conversionFactor;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityOrdered;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitCost;

    // Percent, e.g. 10.00 = 10% — defaults to 0 when not supplied.
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    // Percent — defaults to 0 when not supplied.
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    // Cumulative quantity received across all goods receipts against this
    // line — updated by GoodsReceiptServiceImpl, never edited directly.
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal quantityReceived = BigDecimal.ZERO;
}
