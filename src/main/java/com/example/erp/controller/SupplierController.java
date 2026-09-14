package com.example.erp.controller;

import com.example.erp.dto.AddSupplierNoteRequest;
import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.BalanceAdjustmentRequest;
import com.example.erp.dto.CreateSupplierRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.SupplierActivityFilterRequest;
import com.example.erp.dto.SupplierActivityResponse;
import com.example.erp.dto.SupplierFilterRequest;
import com.example.erp.dto.SupplierResponse;
import com.example.erp.dto.UpdateSupplierRequest;
import com.example.erp.dto.UpdateSupplierStatusRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Admin-only supplier management. Status changes and balance adjustments are
// attributed to the acting admin and logged as SupplierActivity entries (see
// GET .../{id}/activities — "Supplier history"). Mirrors CustomerController.
@RestController
@RequestMapping("/api/admin/suppliers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<PageResponse<SupplierResponse>> list(@ModelAttribute SupplierFilterRequest filter) {
        return ResponseEntity.ok(supplierService.listSuppliers(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getSupplier(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupplierResponse>> create(@Valid @RequestBody CreateSupplierRequest request,
                                                                   Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Supplier created", supplierService.createSupplier(request, requireUsername(authentication))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> update(@PathVariable Long id,
                                                                   @Valid @RequestBody UpdateSupplierRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Supplier updated", supplierService.updateSupplier(id, request)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateStatus(@PathVariable Long id,
                                                                         @Valid @RequestBody UpdateSupplierStatusRequest request,
                                                                         Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                supplierService.updateStatus(id, request, requireUsername(authentication))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier deleted", null));
    }

    @PostMapping("/{id}/balance-adjustments")
    public ResponseEntity<ApiResponse<SupplierResponse>> adjustBalance(@PathVariable Long id,
                                                                          @Valid @RequestBody BalanceAdjustmentRequest request,
                                                                          Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Balance adjusted",
                supplierService.adjustBalance(id, request, requireUsername(authentication))));
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<PageResponse<SupplierActivityResponse>> listActivities(@PathVariable Long id,
                                                                                    @ModelAttribute SupplierActivityFilterRequest filter) {
        return ResponseEntity.ok(supplierService.listActivities(id, filter));
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<ApiResponse<SupplierActivityResponse>> addNote(@PathVariable Long id,
                                                                            @Valid @RequestBody AddSupplierNoteRequest request,
                                                                            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Note added", supplierService.addNote(id, request, requireUsername(authentication))));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
