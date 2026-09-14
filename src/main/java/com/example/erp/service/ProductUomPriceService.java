package com.example.erp.service;

import com.example.erp.dto.ProductUomPriceRequest;
import com.example.erp.dto.ProductUomPriceResponse;

import java.util.List;

public interface ProductUomPriceService {

    List<ProductUomPriceResponse> list(Long productId, Long productUomId);

    ProductUomPriceResponse create(Long productId, Long productUomId, ProductUomPriceRequest request);

    ProductUomPriceResponse update(Long productId, Long productUomId, Long id, ProductUomPriceRequest request);

    void delete(Long productId, Long productUomId, Long id);
}
