package com.example.erp.service;

import com.example.erp.dto.CreatePettyCashEntryRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PettyCashEntryFilterRequest;
import com.example.erp.dto.PettyCashEntryResponse;
import com.example.erp.dto.PettyCashSummaryResponse;

public interface PettyCashService {

    PageResponse<PettyCashEntryResponse> listEntries(PettyCashEntryFilterRequest filter);

    PettyCashEntryResponse getEntry(Long id);

    PettyCashEntryResponse createEntry(CreatePettyCashEntryRequest request, String actingUsername);

    PettyCashSummaryResponse getSummary(Long companyId);
}
