package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductVariantFilterRequest;
import com.example.erp.dto.ProductVariantRequest;
import com.example.erp.dto.ProductVariantResponse;
import com.example.erp.service.ProductVariantService;
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
@RequestMapping("/api/admin/product-variants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductVariantController {

    private final ProductVariantService service;

    @GetMapping
    public ResponseEntity<PageResponse<ProductVariantResponse>> list(@ModelAttribute ProductVariantFilterRequest filter) {
        return ResponseEntity.ok(service.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductVariantResponse>> create(@Valid @RequestBody ProductVariantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Variant created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> update(@PathVariable Long id,
                                                                         @Valid @RequestBody ProductVariantRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Variant updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Variant deleted", null));
    }
}
