package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductPriceFilterRequest;
import com.example.erp.dto.ProductPriceRequest;
import com.example.erp.dto.ProductPriceResponse;
import com.example.erp.service.ProductPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Per-price-group pricing overrides for a product — e.g. a lower price for
// the Wholesale tier. Falls back to Product.sellingPrice when no override
// exists (see SalesOrderServiceImpl's pricing cascade).
@RestController
@RequestMapping("/api/admin/product-prices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class ProductPriceController {

    private final ProductPriceService service;

    @GetMapping
    public ResponseEntity<PageResponse<ProductPriceResponse>> list(@ModelAttribute ProductPriceFilterRequest filter) {
        return ResponseEntity.ok(service.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductPriceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductPriceResponse>> create(@Valid @RequestBody ProductPriceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Product price created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductPriceResponse>> update(@PathVariable Long id,
                                                                        @Valid @RequestBody ProductPriceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Product price updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Product price deleted", null));
    }
}
