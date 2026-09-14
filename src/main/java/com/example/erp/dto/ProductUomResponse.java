package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUomResponse {

    private Long id;
    private Long productId;
    // Null when this row belongs to the product itself rather than one of its variants.
    private Long variantId;
    private Long unitOfMeasureId;
    private String unitOfMeasureName;
    private String unitOfMeasureAbbreviation;
    private BigDecimal conversionFactor;
    private boolean baseUnit;
    private boolean allowPurchase;
    private boolean allowSales;
    private boolean allowInventory;
    private boolean defaultPurchase;
    private boolean defaultSales;
    private String barcode;
    // Resolution order: an active, currently-effective ProductUomPrice for
    // the requested priceGroupId, else this row's own flat price override,
    // else (product/variant) sellingPrice * conversionFactor.
    private BigDecimal effectivePrice;
    // Non-null only when effectivePrice came from a ProductUomPrice match —
    // i.e. the priceGroupId that was actually requested and had one.
    private Long priceGroupId;
    private String priceGroupName;
    private BigDecimal price;
    private boolean active;
}
