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

@Entity
@Table(name = "units_of_measure")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitOfMeasure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Short display form, e.g. "kg", "pcs", "L".
    @Column(nullable = false)
    private String abbreviation;

    private String description;

    // Whether a fractional quantity is valid for this unit (e.g. 2.5 KG is
    // fine, 2.5 PCS is not). Boxed Boolean, deliberately nullable — same
    // ddl-auto=update reason as baseUnit below; treat a null read as true
    // (most units, e.g. weight/volume/length, do allow decimals).
    @Column(name = "decimal_allowed")
    private Boolean decimalAllowed;

    @Builder.Default
    private boolean active = true;

    // Optional — units without a category can't be generically converted,
    // only used directly or through a product-specific
    // ProductUom.conversionFactor.
    @Column(name = "category_id")
    private Long categoryId;

    // Exactly one base unit per category — see
    // UnitOfMeasureServiceImpl.setCategoryBaseUnit, which also keeps
    // UomCategory.baseUnitId pointed at it. Boxed Boolean (not primitive),
    // deliberately nullable — same ddl-auto=update reason as
    // Product.trackingType: this column was added to an already-populated
    // table, so existing rows read back as null, and a primitive can't hold
    // that. Treat a null read as false (see UnitOfMeasureServiceImpl).
    @Column(name = "is_base_unit")
    @Builder.Default
    private Boolean baseUnit = false;

    // A non-base unit's factor to its category's base unit lives in a
    // UomConversion row (fromUnitOfMeasureId = this unit, toUnitOfMeasureId =
    // the category's base unit) rather than as a column here — see
    // UomConversion.
}
