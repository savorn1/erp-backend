package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateStockTransferRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ShipStockTransferRequest;
import com.example.erp.dto.StockTransferFilterRequest;
import com.example.erp.dto.StockTransferResponse;
import com.example.erp.exception.AppException;
import com.example.erp.service.StockTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stock-transfers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class StockTransferController {

    private final StockTransferService stockTransferService;

    @GetMapping
    public ResponseEntity<PageResponse<StockTransferResponse>> list(@ModelAttribute StockTransferFilterRequest filter) {
        return ResponseEntity.ok(stockTransferService.listStockTransfers(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockTransferResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(stockTransferService.getStockTransfer(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StockTransferResponse>> create(@Valid @RequestBody CreateStockTransferRequest request,
                                                                         Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock transfer requested", stockTransferService.createStockTransfer(request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<StockTransferResponse>> approve(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Stock transfer approved", stockTransferService.approveStockTransfer(id, requireUsername(authentication))));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<StockTransferResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Stock transfer rejected", stockTransferService.rejectStockTransfer(id)));
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<ApiResponse<StockTransferResponse>> ship(@PathVariable Long id,
                                                                       @Valid @RequestBody(required = false) ShipStockTransferRequest request,
                                                                       Authentication authentication) {
        ShipStockTransferRequest body = request == null ? new ShipStockTransferRequest() : request;
        return ResponseEntity.ok(ApiResponse.success("Stock transfer shipped", stockTransferService.shipStockTransfer(id, body, requireUsername(authentication))));
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<ApiResponse<StockTransferResponse>> receive(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Stock transfer received", stockTransferService.receiveStockTransfer(id, requireUsername(authentication))));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<StockTransferResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Stock transfer cancelled", stockTransferService.cancelStockTransfer(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        stockTransferService.deleteStockTransfer(id);
        return ResponseEntity.ok(ApiResponse.success("Stock transfer deleted", null));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
