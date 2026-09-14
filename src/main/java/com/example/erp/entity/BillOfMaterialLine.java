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
@Table(name = "bill_of_material_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillOfMaterialLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bom_id", nullable = false)
    private Long bomId;

    // The raw material/component consumed — a plain Product row, since this
    // codebase has no distinct "raw material" entity (see Product's own
    // comment on typeId).
    @Column(name = "component_product_id", nullable = false)
    private Long componentProductId;

    // Quantity consumed per one run of the parent BOM (i.e. per
    // outputQuantity of finished goods), in the component's own base unit.
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    // Expected wastage for this component, added on top of quantity when a
    // ManufacturingOrder's material requirement is computed. 0-100.
    @Column(name = "scrap_percent", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal scrapPercent = BigDecimal.ZERO;
}
