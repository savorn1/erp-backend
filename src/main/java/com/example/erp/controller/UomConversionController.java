package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.ConvertUnitsRequest;
import com.example.erp.dto.ConvertUnitsResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UomConversionFilterRequest;
import com.example.erp.dto.UomConversionRequest;
import com.example.erp.dto.UomConversionResponse;
import com.example.erp.service.UomConversionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/uom-conversions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UomConversionController {

    private final UomConversionService service;

    @GetMapping
    public ResponseEntity<PageResponse<UomConversionResponse>> list(@ModelAttribute UomConversionFilterRequest filter) {
        return ResponseEntity.ok(service.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UomConversionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UomConversionResponse>> create(@Valid @RequestBody UomConversionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("UOM conversion created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UomConversionResponse>> update(@PathVariable Long id, @Valid @RequestBody UomConversionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("UOM conversion updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("UOM conversion deleted", null));
    }

    @GetMapping("/convert")
    public ResponseEntity<ApiResponse<ConvertUnitsResponse>> convert(@Valid @ModelAttribute ConvertUnitsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.convert(request)));
    }
}
