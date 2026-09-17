package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.InventorySettingsRequest;
import com.example.erp.dto.InventorySettingsResponse;
import com.example.erp.service.InventorySettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inventory-settings")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class InventorySettingsController {

    private final InventorySettingsService inventorySettingsService;

    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<InventorySettingsResponse>> getForCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success(inventorySettingsService.getForCompany(companyId)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<InventorySettingsResponse>> upsert(@Valid @RequestBody InventorySettingsRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Inventory settings saved", inventorySettingsService.upsert(request)));
    }
}
