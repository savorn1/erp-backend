package com.example.erp.controller;

import com.example.erp.dto.AddCustomerNoteRequest;
import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.BalanceAdjustmentRequest;
import com.example.erp.dto.CreateCustomerRequest;
import com.example.erp.dto.CustomerActivityFilterRequest;
import com.example.erp.dto.CustomerActivityResponse;
import com.example.erp.dto.CustomerFilterRequest;
import com.example.erp.dto.CustomerResponse;
import com.example.erp.dto.ImportResultResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateCustomerRequest;
import com.example.erp.dto.UpdateCustomerStatusRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Admin-only customer management. Status changes and balance adjustments are
// attributed to the acting admin and logged as CustomerActivity entries (see
// GET .../{id}/activities — "Customer history").
@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<PageResponse<CustomerResponse>> list(@ModelAttribute CustomerFilterRequest filter) {
        return ResponseEntity.ok(customerService.listCustomers(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomer(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CreateCustomerRequest request,
                                                                   Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created", customerService.createCustomer(request, requireUsername(authentication))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(@PathVariable Long id,
                                                                   @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Customer updated", customerService.updateCustomer(id, request)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateStatus(@PathVariable Long id,
                                                                         @Valid @RequestBody UpdateCustomerStatusRequest request,
                                                                         Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                customerService.updateStatus(id, request, requireUsername(authentication))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted", null));
    }

    @PostMapping("/{id}/balance-adjustments")
    public ResponseEntity<ApiResponse<CustomerResponse>> adjustBalance(@PathVariable Long id,
                                                                          @Valid @RequestBody BalanceAdjustmentRequest request,
                                                                          Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Balance adjusted",
                customerService.adjustBalance(id, request, requireUsername(authentication))));
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<PageResponse<CustomerActivityResponse>> listActivities(@PathVariable Long id,
                                                                                    @ModelAttribute CustomerActivityFilterRequest filter) {
        return ResponseEntity.ok(customerService.listActivities(id, filter));
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<ApiResponse<CustomerActivityResponse>> addNote(@PathVariable Long id,
                                                                            @Valid @RequestBody AddCustomerNoteRequest request,
                                                                            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Note added", customerService.addNote(id, request, requireUsername(authentication))));
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importCsv(@RequestParam("file") MultipartFile file,
                                                                        @RequestParam("companyId") Long companyId,
                                                                        Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                customerService.importCustomersFromCsv(file, companyId, requireUsername(authentication))));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
