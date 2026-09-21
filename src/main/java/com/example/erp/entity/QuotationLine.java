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
@Table(name = "quotation_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quotation_id", nullable = false)
    private Long quotationId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    // The unit `quantity` is expressed in — e.g. quoting "10 BOX" of a product
    // whose inventory unit is PCS. Nullable: a null read means the product's own
    // base unit at factor 1 (see QuotationServiceImpl.resolveLineUnit).
    @Column(name = "unit_of_measure_id")
    private Long unitOfMeasureId;

    // Snapshotted from ProductUom.conversionFactor when the quote is saved, and
    // carried onto the sales order if the quote is converted.
    @Column(name = "conversion_factor", precision = 19, scale = 6)
    private BigDecimal conversionFactor;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;
}
