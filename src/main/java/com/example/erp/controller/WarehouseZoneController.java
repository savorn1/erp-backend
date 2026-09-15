package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.WarehouseZoneFilterRequest;
import com.example.erp.dto.WarehouseZoneRequest;
import com.example.erp.dto.WarehouseZoneResponse;
import com.example.erp.service.WarehouseZoneService;
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
@RequestMapping("/api/admin/warehouse-zones")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class WarehouseZoneController {

    private final WarehouseZoneService service;

    @GetMapping
    public ResponseEntity<PageResponse<WarehouseZoneResponse>> list(@ModelAttribute WarehouseZoneFilterRequest filter) {
        return ResponseEntity.ok(service.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseZoneResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WarehouseZoneResponse>> create(@Valid @RequestBody WarehouseZoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Zone created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseZoneResponse>> update(@PathVariable Long id,
                                                                        @Valid @RequestBody WarehouseZoneRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Zone updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Zone deleted", null));
    }
}
