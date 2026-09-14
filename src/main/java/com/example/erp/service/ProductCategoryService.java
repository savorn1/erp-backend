package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductCategoryFilterRequest;
import com.example.erp.dto.ProductCategoryRequest;
import com.example.erp.dto.ProductCategoryResponse;

public interface ProductCategoryService {

    PageResponse<ProductCategoryResponse> list(ProductCategoryFilterRequest filter);

    ProductCategoryResponse get(Long id);

    ProductCategoryResponse create(ProductCategoryRequest request);

    ProductCategoryResponse update(Long id, ProductCategoryRequest request);

    void delete(Long id);
}
