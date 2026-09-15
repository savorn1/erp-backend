package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateGoodsReceiptRequest;
import com.example.erp.dto.GoodsReceiptFilterRequest;
import com.example.erp.dto.GoodsReceiptResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.QualityCheckRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.GoodsReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// No PUT/DELETE — a posted receipt is an immutable ledger entry (see
// GoodsReceipt's own comment): correcting a mistake needs a separate
// reversal/adjustment mechanism, out of scope for this step.
@RestController
@RequestMapping("/api/admin/goods-receipts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;

    @GetMapping
    public ResponseEntity<PageResponse<GoodsReceiptResponse>> list(@ModelAttribute GoodsReceiptFilterRequest filter) {
        return ResponseEntity.ok(goodsReceiptService.listGoodsReceipts(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GoodsReceiptResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(goodsReceiptService.getGoodsReceipt(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GoodsReceiptResponse>> create(@Valid @RequestBody CreateGoodsReceiptRequest request,
                                                                       Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goods receipt posted", goodsReceiptService.createGoodsReceipt(request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/lines/{lineId}/quality-check")
    public ResponseEntity<ApiResponse<GoodsReceiptResponse>> qualityCheck(@PathVariable Long id, @PathVariable Long lineId,
                                                                              @Valid @RequestBody QualityCheckRequest request,
                                                                              Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Quality check recorded",
                goodsReceiptService.recordQualityCheck(id, lineId, request, requireUsername(authentication))));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
