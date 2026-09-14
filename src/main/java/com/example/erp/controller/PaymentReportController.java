package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CollectionByCustomerResponse;
import com.example.erp.dto.CollectionBySalespersonResponse;
import com.example.erp.dto.PaymentByBranchResponse;
import com.example.erp.dto.PaymentByMethodResponse;
import com.example.erp.dto.PaymentDetailResponse;
import com.example.erp.dto.PaymentReconciliationResponse;
import com.example.erp.dto.PaymentReportFilterRequest;
import com.example.erp.dto.PaymentSummaryResponse;
import com.example.erp.dto.RefundReportResponse;
import com.example.erp.dto.TransferReportResponse;
import com.example.erp.service.PaymentReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/payment-reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PaymentReportController {

    private final PaymentReportService paymentReportService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PaymentSummaryResponse>> summary(@ModelAttribute PaymentReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(paymentReportService.summary(filter)));
    }

    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<PaymentDetailResponse>> detail(@ModelAttribute PaymentReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(paymentReportService.detail(filter)));
    }

    @GetMapping("/by-method")
    public ResponseEntity<ApiResponse<PaymentByMethodResponse>> byMethod(@ModelAttribute PaymentReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(paymentReportService.byMethod(filter)));
    }

    @GetMapping("/by-branch")
    public ResponseEntity<ApiResponse<PaymentByBranchResponse>> byBranch(@ModelAttribute PaymentReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(paymentReportService.byBranch(filter)));
    }

    @GetMapping("/refunds")
    public ResponseEntity<ApiResponse<RefundReportResponse>> refunds(@ModelAttribute PaymentReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(paymentReportService.refunds(filter)));
    }

    @GetMapping("/transfers")
    public ResponseEntity<ApiResponse<TransferReportResponse>> transfers(@ModelAttribute PaymentReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(paymentReportService.transfers(filter)));
    }

    @GetMapping("/collection-by-customer")
    public ResponseEntity<ApiResponse<CollectionByCustomerResponse>> collectionByCustomer(@ModelAttribute PaymentReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(paymentReportService.collectionByCustomer(filter)));
    }

    @GetMapping("/collection-by-salesperson")
    public ResponseEntity<ApiResponse<CollectionBySalespersonResponse>> collectionBySalesperson(@ModelAttribute PaymentReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(paymentReportService.collectionBySalesperson(filter)));
    }

    @GetMapping("/reconciliation")
    public ResponseEntity<ApiResponse<PaymentReconciliationResponse>> reconciliation(@ModelAttribute PaymentReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(paymentReportService.reconciliation(filter)));
    }
}
