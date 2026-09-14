package com.example.erp.service;

import com.example.erp.dto.AccountFilterRequest;
import com.example.erp.dto.AccountRequest;
import com.example.erp.dto.AccountResponse;
import com.example.erp.dto.PageResponse;

import java.util.List;

public interface AccountService {

    PageResponse<AccountResponse> list(AccountFilterRequest filter);

    AccountResponse get(Long id);

    AccountResponse create(AccountRequest request);

    AccountResponse update(Long id, AccountRequest request);

    // Refuses when the account has children — reassign or delete them first.
    void delete(Long id);

    // Idempotent — creates the standard three-level chart (1000 Assets >
    // 1100 Current Assets / 1200 Fixed Assets > leaf accounts, 2000
    // Liabilities, 3000 Equity, 4000 Revenue, 5000 Expenses) for the given
    // company. Any account code that already exists there is left exactly
    // as-is and only the missing ones are added beneath it.
    List<AccountResponse> seedSampleChartOfAccounts(Long companyId);
}
