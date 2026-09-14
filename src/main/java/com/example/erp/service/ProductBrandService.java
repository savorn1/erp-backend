package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductBrandFilterRequest;
import com.example.erp.dto.ProductBrandRequest;
import com.example.erp.dto.ProductBrandResponse;

public interface ProductBrandService {

    PageResponse<ProductBrandResponse> list(ProductBrandFilterRequest filter);

    ProductBrandResponse get(Long id);

    ProductBrandResponse create(ProductBrandRequest request);

    ProductBrandResponse update(Long id, ProductBrandRequest request);

    void delete(Long id);
}
