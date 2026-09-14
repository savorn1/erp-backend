package com.example.erp.service;

import com.example.erp.dto.CreatePurchaseCreditNoteRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PurchaseCreditNoteFilterRequest;
import com.example.erp.dto.PurchaseCreditNoteResponse;

public interface PurchaseCreditNoteService {

    PageResponse<PurchaseCreditNoteResponse> listPurchaseCreditNotes(PurchaseCreditNoteFilterRequest filter);

    PurchaseCreditNoteResponse getPurchaseCreditNote(Long id);

    PurchaseCreditNoteResponse createPurchaseCreditNote(Long purchaseInvoiceId, CreatePurchaseCreditNoteRequest request, String actingUsername);
}
