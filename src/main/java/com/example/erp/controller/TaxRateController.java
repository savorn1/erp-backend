package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.TaxRateFilterRequest;
import com.example.erp.dto.TaxRateRequest;
import com.example.erp.dto.TaxRateResponse;
import com.example.erp.service.TaxRateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tax-rates")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class TaxRateController {

    private final TaxRateService taxRateService;

    @GetMapping
    public ResponseEntity<PageResponse<TaxRateResponse>> list(@ModelAttribute TaxRateFilterRequest filter) {
        return ResponseEntity.ok(taxRateService.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaxRateResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(taxRateService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaxRateResponse>> create(@Valid @RequestBody TaxRateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tax rate created", taxRateService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaxRateResponse>> update(@PathVariable Long id, @Valid @RequestBody TaxRateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tax rate updated", taxRateService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        taxRateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Tax rate deleted", null));
    }
}
