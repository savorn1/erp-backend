package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.DepreciationEntryFilterRequest;
import com.example.erp.dto.DepreciationEntryResponse;
import com.example.erp.dto.DepreciationRunResponse;
import com.example.erp.dto.DisposeFixedAssetRequest;
import com.example.erp.dto.FixedAssetFilterRequest;
import com.example.erp.dto.FixedAssetRequest;
import com.example.erp.dto.FixedAssetResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RunDepreciationRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.FixedAssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/fixed-assets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class FixedAssetController {

    private final FixedAssetService fixedAssetService;

    @GetMapping
    public ResponseEntity<PageResponse<FixedAssetResponse>> list(@ModelAttribute FixedAssetFilterRequest filter) {
        return ResponseEntity.ok(fixedAssetService.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FixedAssetResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(fixedAssetService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FixedAssetResponse>> create(@Valid @RequestBody FixedAssetRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fixed asset created", fixedAssetService.create(request, requireUsername(authentication))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FixedAssetResponse>> update(@PathVariable Long id, @Valid @RequestBody FixedAssetRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Fixed asset updated", fixedAssetService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, Authentication authentication) {
        fixedAssetService.delete(id, requireUsername(authentication));
        return ResponseEntity.ok(ApiResponse.success("Fixed asset deleted", null));
    }

    @PostMapping("/{id}/dispose")
    public ResponseEntity<ApiResponse<FixedAssetResponse>> dispose(@PathVariable Long id, @Valid @RequestBody DisposeFixedAssetRequest request,
                                                                     Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Fixed asset disposed", fixedAssetService.dispose(id, request, requireUsername(authentication))));
    }

    @PostMapping("/depreciation-runs")
    public ResponseEntity<ApiResponse<DepreciationRunResponse>> runDepreciation(@Valid @RequestBody RunDepreciationRequest request,
                                                                                  Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Depreciation posted", fixedAssetService.runDepreciation(request, requireUsername(authentication))));
    }

    @GetMapping("/depreciation-entries")
    public ResponseEntity<PageResponse<DepreciationEntryResponse>> listDepreciationEntries(@ModelAttribute DepreciationEntryFilterRequest filter) {
        return ResponseEntity.ok(fixedAssetService.listDepreciationEntries(filter));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
