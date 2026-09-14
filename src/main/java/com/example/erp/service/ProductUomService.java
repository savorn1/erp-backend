package com.example.erp.service;

import com.example.erp.dto.ConvertUomRequest;
import com.example.erp.dto.ConvertUomResponse;
import com.example.erp.dto.ProductUomRequest;
import com.example.erp.dto.ProductUomResponse;

import java.util.List;

public interface ProductUomService {

    // variantId null scopes to the product itself; set, scopes to one of its
    // ProductVariant rows instead — each scope has its own independent set of
    // UOM rows. Always includes the base-unit row for that scope, auto-
    // creating it the first time it's looked at if it doesn't exist yet.
    // priceGroupId is optional — when given, effectivePrice on each row
    // resolves through that price group's ProductUomPrice first (see
    // ProductUomServiceImpl.resolveEffectivePrice); when null, effectivePrice
    // falls back to the row's own flat price/conversion as before.
    List<ProductUomResponse> list(Long productId, Long variantId, Long priceGroupId);

    ProductUomResponse create(Long productId, Long variantId, ProductUomRequest request);

    ProductUomResponse update(Long productId, Long variantId, Long id, ProductUomRequest request);

    // Refuses to delete the base-unit row — every scope must always have one.
    void delete(Long productId, Long variantId, Long id);

    // Converts a quantity between two registered UOMs of the same scope
    // (request.variantId null = the product itself, set = that variant) via
    // its base unit. Both UOMs must already be registered ProductUom rows in
    // that scope — see ProductUomServiceImpl.
    ConvertUomResponse convert(ConvertUomRequest request);
}
