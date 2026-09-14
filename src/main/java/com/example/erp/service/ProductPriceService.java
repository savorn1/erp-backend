package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductPriceFilterRequest;
import com.example.erp.dto.ProductPriceRequest;
import com.example.erp.dto.ProductPriceResponse;

public interface ProductPriceService {

    PageResponse<ProductPriceResponse> list(ProductPriceFilterRequest filter);

    ProductPriceResponse get(Long id);

    ProductPriceResponse create(ProductPriceRequest request);

    ProductPriceResponse update(Long id, ProductPriceRequest request);

    void delete(Long id);
}
