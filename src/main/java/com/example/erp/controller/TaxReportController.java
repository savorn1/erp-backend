package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.TaxByCustomerResponse;
import com.example.erp.dto.TaxByProductResponse;
import com.example.erp.dto.TaxBySupplierResponse;
import com.example.erp.dto.TaxDetailResponse;
import com.example.erp.dto.TaxReportFilterRequest;
import com.example.erp.dto.TaxReportResponse;
import com.example.erp.service.TaxReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tax-report")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class TaxReportController {

    private final TaxReportService taxReportService;

    @GetMapping
    public ResponseEntity<ApiResponse<TaxReportResponse>> generate(@ModelAttribute TaxReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(taxReportService.generate(filter)));
    }

    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<TaxDetailResponse>> detail(@ModelAttribute TaxReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(taxReportService.detail(filter)));
    }

    @GetMapping("/by-customer")
    public ResponseEntity<ApiResponse<TaxByCustomerResponse>> byCustomer(@ModelAttribute TaxReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(taxReportService.byCustomer(filter)));
    }

    @GetMapping("/by-supplier")
    public ResponseEntity<ApiResponse<TaxBySupplierResponse>> bySupplier(@ModelAttribute TaxReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(taxReportService.bySupplier(filter)));
    }

    @GetMapping("/by-product")
    public ResponseEntity<ApiResponse<TaxByProductResponse>> byProduct(@ModelAttribute TaxReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(taxReportService.byProduct(filter)));
    }
}
