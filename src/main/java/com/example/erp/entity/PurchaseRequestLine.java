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
@Table(name = "purchase_request_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequestLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_request_id", nullable = false)
    private Long purchaseRequestId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    // The unit `quantity` is expressed in — e.g. requesting "10 BOX" of a
    // product whose inventory unit is PCS. Nullable — added to an
    // already-populated table; a null read means "the product's own base unit,
    // factor 1" (see PurchaseRequestServiceImpl.resolveLineUnit).
    @Column(name = "unit_of_measure_id")
    private Long unitOfMeasureId;

    // Snapshotted from ProductUom.conversionFactor when the request is saved —
    // how many of the product's base unit equal 1 of unitOfMeasureId above.
    // Frozen here rather than re-looked-up so editing that ProductUom later
    // can't silently change what an already-submitted request asked for.
    @Column(name = "conversion_factor", precision = 19, scale = 6)
    private BigDecimal conversionFactor;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    private String notes;
}
