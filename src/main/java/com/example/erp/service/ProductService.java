package com.example.erp.service;

import com.example.erp.dto.CreateProductRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductFilterRequest;
import com.example.erp.dto.ProductResponse;
import com.example.erp.dto.UpdateProductRequest;
import com.example.erp.dto.UpdateProductStatusRequest;

public interface ProductService {

    PageResponse<ProductResponse> listProducts(ProductFilterRequest filter);

    ProductResponse getProduct(Long id);

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(Long id, UpdateProductRequest request);

    ProductResponse updateStatus(Long id, UpdateProductStatusRequest request);

    void deleteProduct(Long id);
}
