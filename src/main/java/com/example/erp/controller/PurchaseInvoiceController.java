package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreatePurchaseCreditNoteRequest;
import com.example.erp.dto.CreatePurchaseInvoiceRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PurchaseCreditNoteFilterRequest;
import com.example.erp.dto.PurchaseCreditNoteResponse;
import com.example.erp.dto.PurchaseInvoiceAgingFilterRequest;
import com.example.erp.dto.PurchaseInvoiceAgingReportResponse;
import com.example.erp.dto.PurchaseInvoiceFilterRequest;
import com.example.erp.dto.PurchaseInvoiceResponse;
import com.example.erp.exception.AppException;
import com.example.erp.service.PurchaseCreditNoteService;
import com.example.erp.service.PurchaseInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Purchase invoices are generated from a PurchaseOrder (bills the full
// ordered quantity — a 2-way match) or from a GoodsReceipt (bills only what
// passed quality check on that receipt — a 3-way match), never created with
// manual line items. Approval charges the supplier's balance; a purchase
// credit note against an approved invoice credits it back (see
// PurchaseInvoiceServiceImpl/PurchaseCreditNoteServiceImpl).
@RestController
@RequestMapping("/api/admin/purchase-invoices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class PurchaseInvoiceController {

    private final PurchaseInvoiceService purchaseInvoiceService;
    private final PurchaseCreditNoteService purchaseCreditNoteService;

    @GetMapping
    public ResponseEntity<PageResponse<PurchaseInvoiceResponse>> list(@ModelAttribute PurchaseInvoiceFilterRequest filter) {
        return ResponseEntity.ok(purchaseInvoiceService.listPurchaseInvoices(filter));
    }

    @GetMapping("/aging")
    public ResponseEntity<ApiResponse<PurchaseInvoiceAgingReportResponse>> agingReport(@ModelAttribute PurchaseInvoiceAgingFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(purchaseInvoiceService.agingReport(filter)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseInvoiceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(purchaseInvoiceService.getPurchaseInvoice(id)));
    }

    @PostMapping("/from-purchase-order/{purchaseOrderId}")
    public ResponseEntity<ApiResponse<PurchaseInvoiceResponse>> createFromPurchaseOrder(@PathVariable Long purchaseOrderId,
                                                                                             @Valid @RequestBody CreatePurchaseInvoiceRequest request,
                                                                                             Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase invoice generated from purchase order",
                        purchaseInvoiceService.createFromPurchaseOrder(purchaseOrderId, request, requireUsername(authentication))));
    }

    @PostMapping("/from-goods-receipt/{goodsReceiptId}")
    public ResponseEntity<ApiResponse<PurchaseInvoiceResponse>> createFromGoodsReceipt(@PathVariable Long goodsReceiptId,
                                                                                            @Valid @RequestBody CreatePurchaseInvoiceRequest request,
                                                                                            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase invoice generated from goods receipt",
                        purchaseInvoiceService.createFromGoodsReceipt(goodsReceiptId, request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<PurchaseInvoiceResponse>> approve(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Purchase invoice approved",
                purchaseInvoiceService.approvePurchaseInvoice(id, requireUsername(authentication))));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PurchaseInvoiceResponse>> cancel(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Purchase invoice cancelled",
                purchaseInvoiceService.cancelPurchaseInvoice(id, requireUsername(authentication))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        purchaseInvoiceService.deletePurchaseInvoice(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase invoice deleted", null));
    }

    @GetMapping("/{id}/credit-notes")
    public ResponseEntity<PageResponse<PurchaseCreditNoteResponse>> listCreditNotes(@PathVariable Long id,
                                                                                        @ModelAttribute PurchaseCreditNoteFilterRequest filter) {
        filter.setPurchaseInvoiceId(id);
        return ResponseEntity.ok(purchaseCreditNoteService.listPurchaseCreditNotes(filter));
    }

    @PostMapping("/{id}/credit-notes")
    public ResponseEntity<ApiResponse<PurchaseCreditNoteResponse>> createCreditNote(@PathVariable Long id,
                                                                                        @Valid @RequestBody CreatePurchaseCreditNoteRequest request,
                                                                                        Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase credit note issued",
                        purchaseCreditNoteService.createPurchaseCreditNote(id, request, requireUsername(authentication))));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
