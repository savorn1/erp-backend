package com.example.erp.controller;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.StockLevelFilterRequest;
import com.example.erp.dto.StockLevelResponse;
import com.example.erp.service.StockLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Read-only — StockLevel is only ever mutated internally by goods receipt
// posting (and, in later inventory steps, other stock movements).
@RestController
@RequestMapping("/api/admin/stock-levels")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StockLevelController {

    private final StockLevelService service;

    @GetMapping
    public ResponseEntity<PageResponse<StockLevelResponse>> list(@ModelAttribute StockLevelFilterRequest filter) {
        return ResponseEntity.ok(service.list(filter));
    }
}
