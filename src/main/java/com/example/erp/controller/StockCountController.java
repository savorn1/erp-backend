package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateStockCountRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.StockCountFilterRequest;
import com.example.erp.dto.StockCountResponse;
import com.example.erp.dto.SubmitStockCountRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.StockCountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stock-counts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class StockCountController {

    private final StockCountService stockCountService;

    @GetMapping
    public ResponseEntity<PageResponse<StockCountResponse>> list(@ModelAttribute StockCountFilterRequest filter) {
        return ResponseEntity.ok(stockCountService.listStockCounts(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockCountResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(stockCountService.getStockCount(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StockCountResponse>> create(@Valid @RequestBody CreateStockCountRequest request,
                                                                      Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock count started", stockCountService.createStockCount(request, requireUsername(authentication))));
    }

    @PutMapping("/{id}/counts")
    public ResponseEntity<ApiResponse<StockCountResponse>> submitCounts(@PathVariable Long id, @Valid @RequestBody SubmitStockCountRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Counted quantities saved", stockCountService.submitCounts(id, request)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<StockCountResponse>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Stock count completed", stockCountService.completeStockCount(id)));
    }

    @PostMapping("/{id}/reconcile")
    public ResponseEntity<ApiResponse<StockCountResponse>> reconcile(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Stock count reconciled", stockCountService.reconcileStockCount(id, requireUsername(authentication))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        stockCountService.deleteStockCount(id);
        return ResponseEntity.ok(ApiResponse.success("Stock count deleted", null));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
