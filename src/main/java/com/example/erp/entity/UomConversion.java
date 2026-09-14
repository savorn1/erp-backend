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

// A directed conversion edge between two UnitOfMeasure rows in the same
// UomCategory. 1 unit of fromUnitOfMeasureId equals conversionFactor units
// of toUnitOfMeasureId. UnitOfMeasureServiceImpl maintains one such row per
// non-base unit, pointed at its category's base unit
// (UomCategory.baseUnitId) — see its upsertConversionToBase.
@Entity
@Table(name = "uom_conversions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UomConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_unit_of_measure_id", nullable = false)
    private Long fromUnitOfMeasureId;

    @Column(name = "to_unit_of_measure_id", nullable = false)
    private Long toUnitOfMeasureId;

    @Column(name = "conversion_factor", nullable = false, precision = 19, scale = 6)
    private BigDecimal conversionFactor;

    @Builder.Default
    private boolean active = true;
}
