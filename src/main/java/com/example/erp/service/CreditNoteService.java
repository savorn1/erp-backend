package com.example.erp.service;

import com.example.erp.dto.CreateCreditNoteRequest;
import com.example.erp.dto.CreditNoteFilterRequest;
import com.example.erp.dto.CreditNoteResponse;
import com.example.erp.dto.PageResponse;

public interface CreditNoteService {

    PageResponse<CreditNoteResponse> listCreditNotes(CreditNoteFilterRequest filter);

    CreditNoteResponse getCreditNote(Long id);

    CreditNoteResponse createCreditNote(Long invoiceId, CreateCreditNoteRequest request, String actingUsername);
}
