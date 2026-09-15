package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.WarehouseBinFilterRequest;
import com.example.erp.dto.WarehouseBinRequest;
import com.example.erp.dto.WarehouseBinResponse;
import com.example.erp.service.WarehouseBinService;
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
@RequestMapping("/api/admin/warehouse-bins")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class WarehouseBinController {

    private final WarehouseBinService service;

    @GetMapping
    public ResponseEntity<PageResponse<WarehouseBinResponse>> list(@ModelAttribute WarehouseBinFilterRequest filter) {
        return ResponseEntity.ok(service.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseBinResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WarehouseBinResponse>> create(@Valid @RequestBody WarehouseBinRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Bin created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseBinResponse>> update(@PathVariable Long id,
                                                                       @Valid @RequestBody WarehouseBinRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Bin updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Bin deleted", null));
    }
}
