package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateQuotationRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.QuotationFilterRequest;
import com.example.erp.dto.QuotationResponse;
import com.example.erp.dto.UpdateQuotationRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.QuotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Admin-only quotation management. Quotations are normally created via
// OpportunityController.convertToQuotation, but can also stand alone.
@RestController
@RequestMapping("/api/admin/quotations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class QuotationController {

    private final QuotationService quotationService;

    @GetMapping
    public ResponseEntity<PageResponse<QuotationResponse>> list(@ModelAttribute QuotationFilterRequest filter) {
        return ResponseEntity.ok(quotationService.listQuotations(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuotationResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(quotationService.getQuotation(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<QuotationResponse>> create(@Valid @RequestBody CreateQuotationRequest request,
                                                                     Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quotation created", quotationService.createQuotation(request, requireUsername(authentication))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuotationResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateQuotationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Quotation updated", quotationService.updateQuotation(id, request)));
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<ApiResponse<QuotationResponse>> send(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Quotation sent", quotationService.sendQuotation(id)));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<QuotationResponse>> accept(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Quotation accepted", quotationService.acceptQuotation(id)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<QuotationResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Quotation rejected", quotationService.rejectQuotation(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        quotationService.deleteQuotation(id);
        return ResponseEntity.ok(ApiResponse.success("Quotation deleted", null));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
