package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreditNoteFilterRequest;
import com.example.erp.dto.CreditNoteResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.service.CreditNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Read-only top-level listing — credit notes are created via
// InvoiceController (POST /api/admin/invoices/{id}/credit-notes).
@RestController
@RequestMapping("/api/admin/credit-notes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CreditNoteController {

    private final CreditNoteService creditNoteService;

    @GetMapping
    public ResponseEntity<PageResponse<CreditNoteResponse>> list(@ModelAttribute CreditNoteFilterRequest filter) {
        return ResponseEntity.ok(creditNoteService.listCreditNotes(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CreditNoteResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(creditNoteService.getCreditNote(id)));
    }
}
