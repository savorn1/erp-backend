package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductCategoryFilterRequest;
import com.example.erp.dto.ProductCategoryRequest;
import com.example.erp.dto.ProductCategoryResponse;
import com.example.erp.service.ProductCategoryService;
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
@RequestMapping("/api/admin/product-categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductCategoryController {

    private final ProductCategoryService service;

    @GetMapping
    public ResponseEntity<PageResponse<ProductCategoryResponse>> list(@ModelAttribute ProductCategoryFilterRequest filter) {
        return ResponseEntity.ok(service.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> create(@Valid @RequestBody ProductCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Category created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> update(@PathVariable Long id,
                                                                         @Valid @RequestBody ProductCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Category updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }
}
