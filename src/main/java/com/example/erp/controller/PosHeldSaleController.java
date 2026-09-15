package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PosHeldSaleFilterRequest;
import com.example.erp.dto.PosHeldSaleResponse;
import com.example.erp.dto.PosHoldRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.PosHeldSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/pos-held-sales")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class PosHeldSaleController {

    private final PosHeldSaleService posHeldSaleService;

    @GetMapping
    public ResponseEntity<PageResponse<PosHeldSaleResponse>> list(@ModelAttribute PosHeldSaleFilterRequest filter) {
        return ResponseEntity.ok(posHeldSaleService.list(filter));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PosHeldSaleResponse>> hold(@Valid @RequestBody PosHoldRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sale held", posHeldSaleService.hold(request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<PosHeldSaleResponse>> resume(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(posHeldSaleService.resume(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> discard(@PathVariable Long id) {
        posHeldSaleService.discard(id);
        return ResponseEntity.noContent().build();
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
