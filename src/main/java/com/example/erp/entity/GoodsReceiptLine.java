package com.example.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.time.LocalDateTime;

@Entity
@Table(name = "goods_receipt_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceiptLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "goods_receipt_id", nullable = false)
    private Long goodsReceiptId;

    @Column(name = "purchase_order_line_id", nullable = false)
    private Long purchaseOrderLineId;

    // Denormalized from the PO line at posting time — convenient for display
    // without an extra join, and stable even if the line were ever removed.
    @Column(name = "product_id", nullable = false)
    private Long productId;

    // Denormalized from the PO line at posting time (same reasoning as
    // productId above) — quantityReceived below is expressed in this unit,
    // not necessarily the product's base/inventory unit. Null means the
    // product's own base unit, factor 1.
    @Column(name = "unit_of_measure_id")
    private Long unitOfMeasureId;

    @Column(name = "conversion_factor", precision = 19, scale = 6)
    private BigDecimal conversionFactor;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityReceived;

    // Specific shelf/bin the received stock was placed in — optional.
    @Column(name = "bin_id")
    private Long binId;

    // Set when the product is BATCH- or SERIAL-tracked (see ProductTrackingType
    // and GoodsReceiptServiceImpl's stock validation) — null for NONE-tracked products.
    @Column(name = "batch_id")
    private Long batchId;

    // Stock only moves once this reaches PASSED (see
    // GoodsReceiptServiceImpl.recordQualityCheck) — quantityReceived above is
    // just what was physically counted in, not what's usable.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private QualityCheckStatus qualityStatus = QualityCheckStatus.PENDING;

    @Column(columnDefinition = "text")
    private String qualityNotes;

    private String qualityCheckedBy;

    private LocalDateTime qualityCheckedAt;
}
