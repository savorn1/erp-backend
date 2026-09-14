package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.ProductUomPriceRequest;
import com.example.erp.dto.ProductUomPriceResponse;
import com.example.erp.service.ProductUomPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Per-(ProductUom, PriceGroup) selling price — e.g. a product's BOX unit
// priced differently for Retail vs Wholesale. Foundation only, same as
// ProductUom itself — see ProductUomController's own comment.
@RestController
@RequestMapping("/api/admin/products/{productId}/uoms/{productUomId}/prices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductUomPriceController {

    private final ProductUomPriceService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductUomPriceResponse>>> list(@PathVariable Long productId, @PathVariable Long productUomId) {
        return ResponseEntity.ok(ApiResponse.success(service.list(productId, productUomId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductUomPriceResponse>> create(@PathVariable Long productId, @PathVariable Long productUomId,
                                                                        @Valid @RequestBody ProductUomPriceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Price created", service.create(productId, productUomId, request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductUomPriceResponse>> update(@PathVariable Long productId, @PathVariable Long productUomId,
                                                                        @PathVariable Long id, @Valid @RequestBody ProductUomPriceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Price updated", service.update(productId, productUomId, id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long productId, @PathVariable Long productUomId, @PathVariable Long id) {
        service.delete(productId, productUomId, id);
        return ResponseEntity.ok(ApiResponse.success("Price deleted", null));
    }
}
