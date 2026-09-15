package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PosExchangeFilterRequest;
import com.example.erp.dto.PosExchangeRequest;
import com.example.erp.dto.PosExchangeResponse;
import com.example.erp.exception.AppException;
import com.example.erp.service.PosExchangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/pos-exchanges")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class PosExchangeController {

    private final PosExchangeService posExchangeService;

    @GetMapping
    public ResponseEntity<PageResponse<PosExchangeResponse>> list(@ModelAttribute PosExchangeFilterRequest filter) {
        return ResponseEntity.ok(posExchangeService.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PosExchangeResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(posExchangeService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PosExchangeResponse>> create(@Valid @RequestBody PosExchangeRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Exchange completed", posExchangeService.createExchange(request, requireUsername(authentication))));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
