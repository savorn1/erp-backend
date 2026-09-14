package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PurchaseCreditNoteFilterRequest;
import com.example.erp.dto.PurchaseCreditNoteResponse;
import com.example.erp.service.PurchaseCreditNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Read-only top-level listing — purchase credit notes are created via
// PurchaseInvoiceController (POST /api/admin/purchase-invoices/{id}/credit-notes).
@RestController
@RequestMapping("/api/admin/purchase-credit-notes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PurchaseCreditNoteController {

    private final PurchaseCreditNoteService purchaseCreditNoteService;

    @GetMapping
    public ResponseEntity<PageResponse<PurchaseCreditNoteResponse>> list(@ModelAttribute PurchaseCreditNoteFilterRequest filter) {
        return ResponseEntity.ok(purchaseCreditNoteService.listPurchaseCreditNotes(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseCreditNoteResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(purchaseCreditNoteService.getPurchaseCreditNote(id)));
    }
}
