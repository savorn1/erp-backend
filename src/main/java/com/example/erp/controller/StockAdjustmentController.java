package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateStockAdjustmentRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.StockAdjustmentFilterRequest;
import com.example.erp.dto.StockAdjustmentResponse;
import com.example.erp.exception.AppException;
import com.example.erp.service.StockAdjustmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stock-adjustments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StockAdjustmentController {

    private final StockAdjustmentService stockAdjustmentService;

    @GetMapping
    public ResponseEntity<PageResponse<StockAdjustmentResponse>> list(@ModelAttribute StockAdjustmentFilterRequest filter) {
        return ResponseEntity.ok(stockAdjustmentService.listStockAdjustments(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockAdjustmentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(stockAdjustmentService.getStockAdjustment(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StockAdjustmentResponse>> create(@Valid @RequestBody CreateStockAdjustmentRequest request,
                                                                          Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock adjustment requested", stockAdjustmentService.createStockAdjustment(request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<StockAdjustmentResponse>> approve(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Stock adjustment approved", stockAdjustmentService.approveStockAdjustment(id, requireUsername(authentication))));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<StockAdjustmentResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Stock adjustment rejected", stockAdjustmentService.rejectStockAdjustment(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        stockAdjustmentService.deleteStockAdjustment(id);
        return ResponseEntity.ok(ApiResponse.success("Stock adjustment deleted", null));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
