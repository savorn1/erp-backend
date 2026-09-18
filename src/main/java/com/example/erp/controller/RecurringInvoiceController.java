package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateRecurringInvoiceTemplateRequest;
import com.example.erp.dto.GenerateDueInvoicesResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RecurringInvoiceTemplateFilterRequest;
import com.example.erp.dto.RecurringInvoiceTemplateResponse;
import com.example.erp.dto.UpdateRecurringInvoiceTemplateRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.RecurringInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/recurring-invoices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class RecurringInvoiceController {

    private final RecurringInvoiceService recurringInvoiceService;

    @GetMapping
    public ResponseEntity<PageResponse<RecurringInvoiceTemplateResponse>> list(@ModelAttribute RecurringInvoiceTemplateFilterRequest filter) {
        return ResponseEntity.ok(recurringInvoiceService.listTemplates(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringInvoiceTemplateResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(recurringInvoiceService.getTemplate(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringInvoiceTemplateResponse>> create(@Valid @RequestBody CreateRecurringInvoiceTemplateRequest request,
                                                                                 Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recurring invoice template created",
                        recurringInvoiceService.createTemplate(request, requireUsername(authentication))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringInvoiceTemplateResponse>> update(@PathVariable Long id,
                                                                                 @Valid @RequestBody UpdateRecurringInvoiceTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Recurring invoice template updated",
                recurringInvoiceService.updateTemplate(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        recurringInvoiceService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Recurring invoice template deleted", null));
    }

    @PostMapping("/generate-due")
    public ResponseEntity<ApiResponse<GenerateDueInvoicesResponse>> generateDue(@RequestParam(required = false) Long companyId,
                                                                                 Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                recurringInvoiceService.generateDueInvoices(companyId, requireUsername(authentication))));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
