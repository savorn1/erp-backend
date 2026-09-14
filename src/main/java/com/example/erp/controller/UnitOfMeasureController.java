package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UnitOfMeasureFilterRequest;
import com.example.erp.dto.UnitOfMeasureRequest;
import com.example.erp.dto.UnitOfMeasureResponse;
import com.example.erp.service.UnitOfMeasureService;
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
@RequestMapping("/api/admin/units-of-measure")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UnitOfMeasureController {

    private final UnitOfMeasureService service;

    @GetMapping
    public ResponseEntity<PageResponse<UnitOfMeasureResponse>> list(@ModelAttribute UnitOfMeasureFilterRequest filter) {
        return ResponseEntity.ok(service.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UnitOfMeasureResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UnitOfMeasureResponse>> create(@Valid @RequestBody UnitOfMeasureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Unit of measure created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UnitOfMeasureResponse>> update(@PathVariable Long id,
                                                                        @Valid @RequestBody UnitOfMeasureRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Unit of measure updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Unit of measure deleted", null));
    }
}
