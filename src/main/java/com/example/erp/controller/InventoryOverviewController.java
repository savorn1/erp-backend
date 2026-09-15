package com.example.erp.controller;

import com.example.erp.dto.InventoryOverviewFilterRequest;
import com.example.erp.dto.InventoryOverviewResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.service.InventoryOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inventory-overview")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class InventoryOverviewController {

    private final InventoryOverviewService inventoryOverviewService;

    @GetMapping
    public ResponseEntity<PageResponse<InventoryOverviewResponse>> get(@ModelAttribute InventoryOverviewFilterRequest filter) {
        return ResponseEntity.ok(inventoryOverviewService.getOverview(filter));
    }
}
