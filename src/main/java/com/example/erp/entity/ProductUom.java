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

// One unit a specific product can be transacted in — e.g. a product whose
// base unit (Product.unitOfMeasureId) is "Each" might also have rows here
// for "Box" (conversionFactor 24) and "Pallet" (conversionFactor 480).
// Foundation only for now: nothing in Sales/Purchasing/Inventory transaction
// lines reads this yet (see ProductUomServiceImpl's own comment) — this is
// reference data and a conversion calculator, not wired into quantities on
// PO/SO/Invoice/GoodsReceipt lines.
@Entity
@Table(name = "product_uoms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    // Null: this row belongs to the product itself. Set: this row is scoped
    // to one specific ProductVariant of this product instead, letting a
    // variant carry its own alternate units/barcodes/pricing distinct from
    // the parent product's — see ProductUomServiceImpl's variant-scoped
    // methods.
    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "unit_of_measure_id", nullable = false)
    private Long unitOfMeasureId;

    // How many of the product's base unit (Product.unitOfMeasureId) equal 1
    // of this unit. Always 1 for the base-unit row itself — see
    // ProductUomServiceImpl.
    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal conversionFactor;

    // True for exactly one row per product — the one matching
    // Product.unitOfMeasureId. Denormalized for query convenience even
    // though it's derivable from unitOfMeasureId.
    @Column(name = "is_base_unit")
    @Builder.Default
    private boolean baseUnit = false;

    @Builder.Default
    private boolean allowPurchase = true;

    @Builder.Default
    private boolean allowSales = true;

    @Builder.Default
    private boolean allowInventory = true;

    // At most one default per usage per product — see
    // ProductUomServiceImpl.enforceSingleDefault.
    @Builder.Default
    private boolean defaultPurchase = false;

    @Builder.Default
    private boolean defaultSales = false;

    // Unique across the company (checked against both other ProductUom rows
    // and Product.barcode) — lets a scanner distinguish "1 Each" from "1 Box"
    // of the same product.
    private String barcode;

    // Optional override — a bulk unit's price isn't always exactly
    // conversionFactor * Product.sellingPrice. Null means "compute it that
    // way" (see ProductUomServiceImpl.effectivePrice).
    @Column(precision = 19, scale = 4)
    private BigDecimal price;

    @Builder.Default
    private boolean active = true;
}
