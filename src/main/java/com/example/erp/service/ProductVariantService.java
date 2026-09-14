package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductVariantFilterRequest;
import com.example.erp.dto.ProductVariantRequest;
import com.example.erp.dto.ProductVariantResponse;

public interface ProductVariantService {

    PageResponse<ProductVariantResponse> list(ProductVariantFilterRequest filter);

    ProductVariantResponse get(Long id);

    ProductVariantResponse create(ProductVariantRequest request);

    ProductVariantResponse update(Long id, ProductVariantRequest request);

    void delete(Long id);
}
