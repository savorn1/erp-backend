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

// Groups compatible units of measure (e.g. "Weight": kg/g/lb; "Count":
// each/dozen/box) so UnitOfMeasure rows in the same category can be
// converted through UomConversion rows anchored on baseUnitId below.
@Entity
@Table(name = "uom_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UomCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Short, stable machine-readable key, e.g. "WEIGHT" — unique across the
    // system. Nullable (unlike name) only because this column was added to
    // an already-populated table via ddl-auto=update; new rows always get
    // one (required at the request-DTO level).
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Builder.Default
    private boolean active = true;

    // Denormalized pointer to this category's base UnitOfMeasure, kept in
    // sync by UnitOfMeasureServiceImpl.setCategoryBaseUnit. Every other
    // unit's UomConversion row targets this one. Nullable — added to an
    // already-populated table, and null until a base unit is assigned.
    @Column(name = "base_unit_id")
    private Long baseUnitId;
}
