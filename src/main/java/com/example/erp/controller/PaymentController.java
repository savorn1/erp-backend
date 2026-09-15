package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreatePaymentRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PaymentFilterRequest;
import com.example.erp.dto.PaymentResponse;
import com.example.erp.dto.RefundPaymentRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Recording a payment (or issuing a refund) is an immutable ledger entry —
// no update/delete, mirrors GoodsReceipt/Delivery/CreditNote. Each
// immediately adjusts the customer's balance (see PaymentServiceImpl). A
// payment's own detail response doubles as its receipt — see "Payment
// receipt" in the frontend view modal.
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<PageResponse<PaymentResponse>> list(@ModelAttribute PaymentFilterRequest filter) {
        return ResponseEntity.ok(paymentService.listPayments(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPayment(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> record(@Valid @RequestBody CreatePaymentRequest request,
                                                                   Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded", paymentService.recordPayment(request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refund(@PathVariable Long id,
                                                                   @Valid @RequestBody RefundPaymentRequest request,
                                                                   Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Refund issued", paymentService.refundPayment(id, request, requireUsername(authentication))));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
