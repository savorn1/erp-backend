package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateSupplierPaymentRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RefundSupplierPaymentRequest;
import com.example.erp.dto.SupplierPaymentFilterRequest;
import com.example.erp.dto.SupplierPaymentResponse;
import com.example.erp.exception.AppException;
import com.example.erp.service.SupplierPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Recording a payment (or issuing a refund) is an immutable ledger entry —
// no update/delete, mirrors PaymentController on the accounts-receivable
// side. Each immediately adjusts the supplier's balance (see
// SupplierPaymentServiceImpl). A payment's own detail response doubles as
// its receipt.
@RestController
@RequestMapping("/api/admin/supplier-payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SupplierPaymentController {

    private final SupplierPaymentService supplierPaymentService;

    @GetMapping
    public ResponseEntity<PageResponse<SupplierPaymentResponse>> list(@ModelAttribute SupplierPaymentFilterRequest filter) {
        return ResponseEntity.ok(supplierPaymentService.listPayments(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierPaymentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(supplierPaymentService.getPayment(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupplierPaymentResponse>> record(@Valid @RequestBody CreateSupplierPaymentRequest request,
                                                                           Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded", supplierPaymentService.recordPayment(request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<SupplierPaymentResponse>> refund(@PathVariable Long id,
                                                                           @Valid @RequestBody RefundSupplierPaymentRequest request,
                                                                           Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Refund issued", supplierPaymentService.refundPayment(id, request, requireUsername(authentication))));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
