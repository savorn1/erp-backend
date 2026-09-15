package com.example.erp.controller;

import com.example.erp.dto.ApDetailResponse;
import com.example.erp.dto.ApPaymentResponse;
import com.example.erp.dto.ApReportFilterRequest;
import com.example.erp.dto.ApSummaryResponse;
import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.SupplierBalanceResponse;
import com.example.erp.dto.SupplierStatementResponse;
import com.example.erp.service.ApReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ap-reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class ApReportController {

    private final ApReportService apReportService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ApSummaryResponse>> summary(@ModelAttribute ApReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(apReportService.summary(filter)));
    }

    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<ApDetailResponse>> detail(@ModelAttribute ApReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(apReportService.detail(filter)));
    }

    @GetMapping("/supplier-balance")
    public ResponseEntity<ApiResponse<SupplierBalanceResponse>> supplierBalance(@ModelAttribute ApReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(apReportService.supplierBalance(filter)));
    }

    @GetMapping("/supplier-statement")
    public ResponseEntity<ApiResponse<SupplierStatementResponse>> supplierStatement(@ModelAttribute ApReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(apReportService.supplierStatement(filter)));
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<ApPaymentResponse>> payments(@ModelAttribute ApReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(apReportService.payments(filter)));
    }
}
