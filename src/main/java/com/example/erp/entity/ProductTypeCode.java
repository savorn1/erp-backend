package com.example.erp.entity;

/**
 * The fixed set of product kinds a {@link ProductType} can represent.
 *
 * <p>Deliberately a closed enum rather than free text: the code is what other
 * parts of the system can reason about, so it has to mean the same thing
 * everywhere. The {@code ProductType} row remains user-editable for its display
 * name and active flag — you can rename "Service" to "Services &amp; Labour" —
 * but you can't invent a new kind.
 *
 * <p>Several types may share a kind, and that's intended: "Spare parts" and
 * "Packaging" are both {@code CONSUMABLE} but are worth reporting on separately.
 * The kind says how the system should treat the type; the name says what the
 * business calls it.
 *
 * <p>Note this classifies the type, not the stock behaviour: {@code Product.stockable}
 * stays authoritative for whether a given product moves inventory, because
 * {@code typeId} is optional and a product may have no type at all.
 */
public enum ProductTypeCode {
    /** A finished item held in stock and sold as-is. */
    GOODS,
    /** Held in stock and consumed by manufacturing rather than sold directly. */
    RAW_MATERIAL,
    /** Held in stock but used internally — packaging, supplies, spares. */
    CONSUMABLE,
    /** Bought or sold as a physical thing, but never inventoried — drop-shipped
     *  goods, freight recharges, items expensed straight to a cost account. */
    NON_STOCK,
    /** Sold or bought but never held: labour, delivery charges, subscriptions. */
    SERVICE,
    /** Capitalised rather than stocked — vehicles, equipment, fixtures. Belongs
     *  to the fixed-assets module, not inventory. */
    ASSET
}
