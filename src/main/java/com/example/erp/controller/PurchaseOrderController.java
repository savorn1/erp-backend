package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreatePurchaseOrderRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PurchaseOrderFilterRequest;
import com.example.erp.dto.PurchaseOrderResponse;
import com.example.erp.dto.UpdatePurchaseOrderRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/purchase-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public ResponseEntity<PageResponse<PurchaseOrderResponse>> list(@ModelAttribute PurchaseOrderFilterRequest filter) {
        return ResponseEntity.ok(purchaseOrderService.listPurchaseOrders(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(purchaseOrderService.getPurchaseOrder(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> create(@Valid @RequestBody CreatePurchaseOrderRequest request,
                                                                        Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase order created", purchaseOrderService.createPurchaseOrder(request, requireUsername(authentication))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> update(@PathVariable Long id,
                                                                        @Valid @RequestBody UpdatePurchaseOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Purchase order updated", purchaseOrderService.updatePurchaseOrder(id, request)));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> submit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Purchase order submitted", purchaseOrderService.submitPurchaseOrder(id)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Purchase order approved", purchaseOrderService.approvePurchaseOrder(id)));
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> send(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Purchase order sent", purchaseOrderService.sendPurchaseOrder(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Purchase order cancelled", purchaseOrderService.cancelPurchaseOrder(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        purchaseOrderService.deletePurchaseOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase order deleted", null));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
