package com.example.erp.service;

import com.example.erp.dto.AccountingPeriodFilterRequest;
import com.example.erp.dto.AccountingPeriodResponse;
import com.example.erp.dto.PageResponse;

public interface AccountingPeriodService {

    PageResponse<AccountingPeriodResponse> list(AccountingPeriodFilterRequest filter);

    AccountingPeriodResponse get(Long id);

    // Individually closing a period doesn't close its fiscal year.
    AccountingPeriodResponse close(Long id);

    // Refused when the parent fiscal year is itself CLOSED — reopen the year
    // first (see FiscalYearServiceImpl.reopen, which reopens every period).
    AccountingPeriodResponse reopen(Long id);
}
