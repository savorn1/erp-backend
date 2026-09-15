package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateCreditNoteRequest;
import com.example.erp.dto.CreateInvoiceRequest;
import com.example.erp.dto.CreditNoteFilterRequest;
import com.example.erp.dto.CreditNoteResponse;
import com.example.erp.dto.InvoiceAgingFilterRequest;
import com.example.erp.dto.InvoiceAgingReportResponse;
import com.example.erp.dto.InvoiceFilterRequest;
import com.example.erp.dto.InvoiceResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.exception.AppException;
import com.example.erp.service.CreditNoteService;
import com.example.erp.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Invoices are generated from a SalesOrder (bills the full ordered quantity)
// or from a Delivery (bills only what shipped) — never created with manual
// line items. Approval charges the customer's balance; a credit note against
// an approved invoice credits it back (see InvoiceServiceImpl/CreditNoteServiceImpl).
@RestController
@RequestMapping("/api/admin/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final CreditNoteService creditNoteService;

    @GetMapping
    public ResponseEntity<PageResponse<InvoiceResponse>> list(@ModelAttribute InvoiceFilterRequest filter) {
        return ResponseEntity.ok(invoiceService.listInvoices(filter));
    }

    @GetMapping("/aging")
    public ResponseEntity<ApiResponse<InvoiceAgingReportResponse>> agingReport(@ModelAttribute InvoiceAgingFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.agingReport(filter)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getInvoice(id)));
    }

    @PostMapping("/from-sales-order/{salesOrderId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createFromSalesOrder(@PathVariable Long salesOrderId,
                                                                                 @Valid @RequestBody CreateInvoiceRequest request,
                                                                                 Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice generated from sales order",
                        invoiceService.createFromSalesOrder(salesOrderId, request, requireUsername(authentication))));
    }

    @PostMapping("/from-delivery/{deliveryId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createFromDelivery(@PathVariable Long deliveryId,
                                                                               @Valid @RequestBody CreateInvoiceRequest request,
                                                                               Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice generated from delivery",
                        invoiceService.createFromDelivery(deliveryId, request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<InvoiceResponse>> approve(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Invoice approved", invoiceService.approveInvoice(id, requireUsername(authentication))));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<InvoiceResponse>> cancel(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Invoice cancelled", invoiceService.cancelInvoice(id, requireUsername(authentication))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice deleted", null));
    }

    @GetMapping("/{id}/credit-notes")
    public ResponseEntity<PageResponse<CreditNoteResponse>> listCreditNotes(@PathVariable Long id, @ModelAttribute CreditNoteFilterRequest filter) {
        filter.setInvoiceId(id);
        return ResponseEntity.ok(creditNoteService.listCreditNotes(filter));
    }

    @PostMapping("/{id}/credit-notes")
    public ResponseEntity<ApiResponse<CreditNoteResponse>> createCreditNote(@PathVariable Long id,
                                                                                @Valid @RequestBody CreateCreditNoteRequest request,
                                                                                Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Credit note issued", creditNoteService.createCreditNote(id, request, requireUsername(authentication))));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
