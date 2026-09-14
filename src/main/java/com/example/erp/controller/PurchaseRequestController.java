package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreatePurchaseRequestRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PurchaseRequestFilterRequest;
import com.example.erp.dto.PurchaseRequestResponse;
import com.example.erp.dto.RejectPurchaseRequestRequest;
import com.example.erp.dto.UpdatePurchaseRequestRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.PurchaseRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/purchase-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PurchaseRequestController {

    private final PurchaseRequestService purchaseRequestService;

    @GetMapping
    public ResponseEntity<PageResponse<PurchaseRequestResponse>> list(@ModelAttribute PurchaseRequestFilterRequest filter) {
        return ResponseEntity.ok(purchaseRequestService.listPurchaseRequests(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(purchaseRequestService.getPurchaseRequest(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> create(@Valid @RequestBody CreatePurchaseRequestRequest request,
                                                                          Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase request created", purchaseRequestService.createPurchaseRequest(request, requireUsername(authentication))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> update(@PathVariable Long id,
                                                                          @Valid @RequestBody UpdatePurchaseRequestRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Purchase request updated", purchaseRequestService.updatePurchaseRequest(id, request)));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> submit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Purchase request submitted", purchaseRequestService.submitPurchaseRequest(id)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Purchase request approved", purchaseRequestService.approvePurchaseRequest(id)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<PurchaseRequestResponse>> reject(@PathVariable Long id,
                                                                          @RequestBody(required = false) RejectPurchaseRequestRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Purchase request rejected", purchaseRequestService.rejectPurchaseRequest(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        purchaseRequestService.deletePurchaseRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase request deleted", null));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
