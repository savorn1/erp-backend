package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.FiscalYearFilterRequest;
import com.example.erp.dto.FiscalYearRequest;
import com.example.erp.dto.FiscalYearResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.service.FiscalYearService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/fiscal-years")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FiscalYearController {

    private final FiscalYearService fiscalYearService;

    @GetMapping
    public ResponseEntity<PageResponse<FiscalYearResponse>> list(@ModelAttribute FiscalYearFilterRequest filter) {
        return ResponseEntity.ok(fiscalYearService.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FiscalYearResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(fiscalYearService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FiscalYearResponse>> create(@Valid @RequestBody FiscalYearRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Fiscal year created", fiscalYearService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FiscalYearResponse>> update(@PathVariable Long id, @Valid @RequestBody FiscalYearRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Fiscal year updated", fiscalYearService.update(id, request)));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<FiscalYearResponse>> close(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Fiscal year closed", fiscalYearService.close(id)));
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<ApiResponse<FiscalYearResponse>> reopen(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Fiscal year reopened", fiscalYearService.reopen(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        fiscalYearService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Fiscal year deleted", null));
    }
}
