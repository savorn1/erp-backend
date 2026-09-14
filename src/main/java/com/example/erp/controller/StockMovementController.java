package com.example.erp.controller;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.StockMovementFilterRequest;
import com.example.erp.dto.StockMovementResponse;
import com.example.erp.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Read-only audit trail — written only by GoodsReceiptServiceImpl (and, in
// later inventory steps, other stock-moving actions).
@RestController
@RequestMapping("/api/admin/stock-movements")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StockMovementController {

    private final StockMovementService service;

    @GetMapping
    public ResponseEntity<PageResponse<StockMovementResponse>> list(@ModelAttribute StockMovementFilterRequest filter) {
        return ResponseEntity.ok(service.list(filter));
    }
}
