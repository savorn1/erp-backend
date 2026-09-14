package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductTypeFilterRequest;
import com.example.erp.dto.ProductTypeRequest;
import com.example.erp.dto.ProductTypeResponse;

public interface ProductTypeService {

    PageResponse<ProductTypeResponse> list(ProductTypeFilterRequest filter);

    ProductTypeResponse get(Long id);

    ProductTypeResponse create(ProductTypeRequest request);

    ProductTypeResponse update(Long id, ProductTypeRequest request);

    void delete(Long id);
}
