package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductBrandFilterRequest;
import com.example.erp.dto.ProductBrandRequest;
import com.example.erp.dto.ProductBrandResponse;
import com.example.erp.service.ProductBrandService;
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

@RestController
@RequestMapping("/api/admin/product-brands")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class ProductBrandController {

    private final ProductBrandService service;

    @GetMapping
    public ResponseEntity<PageResponse<ProductBrandResponse>> list(@ModelAttribute ProductBrandFilterRequest filter) {
        return ResponseEntity.ok(service.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductBrandResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductBrandResponse>> create(@Valid @RequestBody ProductBrandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Brand created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductBrandResponse>> update(@PathVariable Long id,
                                                                       @Valid @RequestBody ProductBrandRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Brand updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Brand deleted", null));
    }
}
