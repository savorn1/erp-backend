package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateSalesOrderRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.SalesOrderFilterRequest;
import com.example.erp.dto.SalesOrderResponse;
import com.example.erp.dto.UpdateSalesOrderRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.SalesOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sales-orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @GetMapping
    public ResponseEntity<PageResponse<SalesOrderResponse>> list(@ModelAttribute SalesOrderFilterRequest filter) {
        return ResponseEntity.ok(salesOrderService.listSalesOrders(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(salesOrderService.getSalesOrder(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SalesOrderResponse>> create(@Valid @RequestBody CreateSalesOrderRequest request,
                                                                     Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sales order created", salesOrderService.createSalesOrder(request, requireUsername(authentication))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> update(@PathVariable Long id,
                                                                     @Valid @RequestBody UpdateSalesOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Sales order updated", salesOrderService.updateSalesOrder(id, request)));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> submit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Sales order submitted", salesOrderService.submitSalesOrder(id)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> approve(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Sales order confirmed",
                salesOrderService.approveSalesOrder(id, requireUsername(authentication))));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SalesOrderResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Sales order cancelled", salesOrderService.cancelSalesOrder(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        salesOrderService.deleteSalesOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Sales order deleted", null));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
