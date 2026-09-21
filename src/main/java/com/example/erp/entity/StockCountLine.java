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
@Table(name = "stock_count_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCountLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_count_id", nullable = false)
    private Long stockCountId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "bin_id")
    private Long binId;

    // The unit the counter works in — e.g. count pallets of bottled water
    // rather than bottles. Purely an input/display unit: the three quantities
    // below are always in the product's base inventory unit, so nothing
    // downstream has to know about it.
    //
    // Nullable for the same ddl-auto=update reason as Product.trackingType —
    // a NOT NULL column can't add itself to a table that already has rows.
    // A null unit means the product's own base unit.
    @Column(name = "unit_of_measure_id")
    private Long unitOfMeasureId;

    // How many base units one of the above makes, snapshotted at creation so
    // that editing the product's UOM setup later can't retroactively change
    // what a historical count meant. Null on pre-UOM rows; treat as 1.
    @Column(name = "conversion_factor", precision = 19, scale = 6)
    private BigDecimal conversionFactor;

    // Snapshotted from StockLevel when the count is created — the baseline
    // the physical count is compared against. In base units.
    //
    // Base rather than the line's unit deliberately: on-hand is rarely an
    // exact multiple of the factor (37 loose bottles, 12 to a case), and
    // storing 37/12 = 3.0833 at scale 4 then multiplying back by 12 gives
    // 0.9996 — a reconciliation that misses by a fraction of a unit every
    // time. Only countedQuantity crosses the unit boundary, and it does so by
    // multiplication, which is exact.
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal systemQuantity;

    // Filled in by submitCounts(); null until then. In base units — the
    // submitted figure is in the line's unit and is converted on the way in.
    @Column(precision = 19, scale = 4)
    private BigDecimal countedQuantity;

    // countedQuantity - systemQuantity, set alongside it. In base units, so
    // it can feed the reconciliation adjustment directly.
    @Column(precision = 19, scale = 4)
    private BigDecimal varianceQuantity;
}
