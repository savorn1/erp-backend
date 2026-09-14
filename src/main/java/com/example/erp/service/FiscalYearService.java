package com.example.erp.service;

import com.example.erp.dto.FiscalYearFilterRequest;
import com.example.erp.dto.FiscalYearRequest;
import com.example.erp.dto.FiscalYearResponse;
import com.example.erp.dto.PageResponse;

public interface FiscalYearService {

    PageResponse<FiscalYearResponse> list(FiscalYearFilterRequest filter);

    FiscalYearResponse get(Long id);

    // Also generates one AccountingPeriod per calendar month in the range
    // when request.generateMonthlyPeriods is true.
    FiscalYearResponse create(FiscalYearRequest request);

    FiscalYearResponse update(Long id, FiscalYearRequest request);

    // OPEN -> CLOSED, and closes every period within it.
    FiscalYearResponse close(Long id);

    // CLOSED -> OPEN, and reopens every period within it (including any
    // that had also been closed individually before the year was).
    FiscalYearResponse reopen(Long id);

    // Refuses on a CLOSED year — reopen it first.
    void delete(Long id);
}
