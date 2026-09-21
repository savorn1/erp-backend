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
@Table(name = "rfq_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfqLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rfq_id", nullable = false)
    private Long rfqId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    // The unit `quantity` is expressed in — the unit suppliers are being asked to
    // quote against, carried onto the purchase order when one is awarded.
    // Nullable: a null read means the product's own base unit at factor 1.
    @Column(name = "unit_of_measure_id")
    private Long unitOfMeasureId;

    @Column(name = "conversion_factor", precision = 19, scale = 6)
    private BigDecimal conversionFactor;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;
}
