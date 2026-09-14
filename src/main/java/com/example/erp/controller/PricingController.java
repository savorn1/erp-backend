package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PriceLookupResponse;
import com.example.erp.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Read-only price-lookup tool: given a company/customer/product, explains
// which rule of the pricing cascade (Customer -> CustomerGroup -> PriceGroup
// -> ProductPrice) resolves the unit price — for support/admin verification,
// not part of the sales order flow itself.
@RestController
@RequestMapping("/api/admin/pricing")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PricingController {

    private final PricingService service;

    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<PriceLookupResponse>> lookup(@RequestParam Long companyId,
                                                                     @RequestParam Long customerId,
                                                                     @RequestParam Long productId) {
        return ResponseEntity.ok(ApiResponse.success(service.lookup(companyId, customerId, productId)));
    }
}
