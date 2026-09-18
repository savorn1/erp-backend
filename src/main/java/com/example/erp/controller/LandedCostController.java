package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateLandedCostRequest;
import com.example.erp.dto.LandedCostFilterRequest;
import com.example.erp.dto.LandedCostResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.exception.AppException;
import com.example.erp.service.LandedCostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/landed-costs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class LandedCostController {

    private final LandedCostService landedCostService;

    @GetMapping
    public ResponseEntity<PageResponse<LandedCostResponse>> list(@ModelAttribute LandedCostFilterRequest filter) {
        return ResponseEntity.ok(landedCostService.listLandedCosts(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LandedCostResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(landedCostService.getLandedCost(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LandedCostResponse>> create(@Valid @RequestBody CreateLandedCostRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Landed cost allocated", landedCostService.createLandedCost(request, requireUsername(authentication))));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
