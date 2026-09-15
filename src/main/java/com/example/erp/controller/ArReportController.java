package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.ArBadDebtResponse;
import com.example.erp.dto.ArCollectionResponse;
import com.example.erp.dto.ArDetailResponse;
import com.example.erp.dto.ArReportFilterRequest;
import com.example.erp.dto.ArSummaryResponse;
import com.example.erp.dto.CustomerBalanceResponse;
import com.example.erp.dto.CustomerStatementResponse;
import com.example.erp.service.ArReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ar-reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class ArReportController {

    private final ArReportService arReportService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ArSummaryResponse>> summary(@ModelAttribute ArReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(arReportService.summary(filter)));
    }

    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<ArDetailResponse>> detail(@ModelAttribute ArReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(arReportService.detail(filter)));
    }

    @GetMapping("/customer-balance")
    public ResponseEntity<ApiResponse<CustomerBalanceResponse>> customerBalance(@ModelAttribute ArReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(arReportService.customerBalance(filter)));
    }

    @GetMapping("/customer-statement")
    public ResponseEntity<ApiResponse<CustomerStatementResponse>> customerStatement(@ModelAttribute ArReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(arReportService.customerStatement(filter)));
    }

    @GetMapping("/collections")
    public ResponseEntity<ApiResponse<ArCollectionResponse>> collections(@ModelAttribute ArReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(arReportService.collections(filter)));
    }

    @GetMapping("/bad-debt")
    public ResponseEntity<ApiResponse<ArBadDebtResponse>> badDebt(@ModelAttribute ArReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(arReportService.badDebt(filter)));
    }
}
