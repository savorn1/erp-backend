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

    // Idempotent — creates the standard demo tree (1000 Assets > Cash/Bank/
    // Inventory, 2000 Liabilities > Accounts Payable, 4000 Revenue > Sales
    // Revenue, 5000 Expenses > Salary/Rent) for the given company, skipping
    // any account code that already exists there.
    List<AccountResponse> seedSampleChartOfAccounts(Long companyId);
}
