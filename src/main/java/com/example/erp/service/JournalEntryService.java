package com.example.erp.service;

import com.example.erp.dto.JournalEntryFilterRequest;
import com.example.erp.dto.JournalEntryRequest;
import com.example.erp.dto.JournalEntryResponse;
import com.example.erp.dto.PageResponse;

public interface JournalEntryService {

    PageResponse<JournalEntryResponse> list(JournalEntryFilterRequest filter);

    JournalEntryResponse get(Long id);

    JournalEntryResponse create(JournalEntryRequest request, String actingUsername);

    JournalEntryResponse update(Long id, JournalEntryRequest request);

    void delete(Long id);

    // Approval — DRAFT -> POSTED. Immutable from this point on.
    JournalEntryResponse post(Long id, String actingUsername);

    // Creates and posts a new entry with every line's debit/credit swapped,
    // linked back to this one. Only a POSTED, not-yet-reversed entry can be
    // reversed.
    JournalEntryResponse reverse(Long id, String actingUsername);
}
