package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PosCheckoutRequest;
import com.example.erp.dto.PosSaleFilterRequest;
import com.example.erp.dto.PosSaleResponse;
import com.example.erp.dto.VoidPosSaleRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.PosSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/pos-sales")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class PosSaleController {

    private final PosSaleService posSaleService;

    @GetMapping
    public ResponseEntity<PageResponse<PosSaleResponse>> list(@ModelAttribute PosSaleFilterRequest filter) {
        return ResponseEntity.ok(posSaleService.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PosSaleResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(posSaleService.get(id)));
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<PosSaleResponse>> checkout(@Valid @RequestBody PosCheckoutRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sale completed", posSaleService.checkout(request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/void")
    public ResponseEntity<ApiResponse<PosSaleResponse>> voidSale(@PathVariable Long id, @RequestBody(required = false) VoidPosSaleRequest request,
                                                                    Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Sale voided",
                posSaleService.voidSale(id, request == null ? new VoidPosSaleRequest() : request, requireUsername(authentication))));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
