package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.SupplierTypeFilterRequest;
import com.example.erp.dto.SupplierTypeRequest;
import com.example.erp.dto.SupplierTypeResponse;
import com.example.erp.service.SupplierTypeService;
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
@RequestMapping("/api/admin/supplier-types")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SupplierTypeController {

    private final SupplierTypeService service;

    @GetMapping
    public ResponseEntity<PageResponse<SupplierTypeResponse>> list(@ModelAttribute SupplierTypeFilterRequest filter) {
        return ResponseEntity.ok(service.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierTypeResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupplierTypeResponse>> create(@Valid @RequestBody SupplierTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Supplier type created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierTypeResponse>> update(@PathVariable Long id,
                                                                       @Valid @RequestBody SupplierTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Supplier type updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier type deleted", null));
    }
}
