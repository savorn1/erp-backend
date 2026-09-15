package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.BillOfMaterialFilterRequest;
import com.example.erp.dto.BillOfMaterialResponse;
import com.example.erp.dto.BomVersionRowResponse;
import com.example.erp.dto.CreateBillOfMaterialRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateBillOfMaterialRequest;
import com.example.erp.service.BillOfMaterialService;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/bill-of-materials")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class BillOfMaterialController {

    private final BillOfMaterialService billOfMaterialService;

    @GetMapping
    public ResponseEntity<PageResponse<BillOfMaterialResponse>> list(@ModelAttribute BillOfMaterialFilterRequest filter) {
        return ResponseEntity.ok(billOfMaterialService.listBoms(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillOfMaterialResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(billOfMaterialService.getBom(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BillOfMaterialResponse>> create(@Valid @RequestBody CreateBillOfMaterialRequest request,
                                                                        Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bill of materials created", billOfMaterialService.createBom(request, requireUsername(authentication))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BillOfMaterialResponse>> update(@PathVariable Long id,
                                                                        @Valid @RequestBody UpdateBillOfMaterialRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Bill of materials updated", billOfMaterialService.updateBom(id, request)));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<BillOfMaterialResponse>> activate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Bill of materials activated", billOfMaterialService.activateBom(id)));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<BillOfMaterialResponse>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Bill of materials deactivated", billOfMaterialService.deactivateBom(id)));
    }

    @PostMapping("/{id}/new-version")
    public ResponseEntity<ApiResponse<BillOfMaterialResponse>> newVersion(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("New BOM version created", billOfMaterialService.createNewVersion(id, authentication.getName())));
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<ApiResponse<List<BomVersionRowResponse>>> versions(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(billOfMaterialService.getVersionHistory(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        billOfMaterialService.deleteBom(id);
        return ResponseEntity.ok(ApiResponse.success("Bill of materials deleted", null));
    }

    private String requireUsername(Authentication authentication) {
        return authentication.getName();
    }
}
