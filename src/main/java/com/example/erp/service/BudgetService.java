package com.example.erp.service;

import com.example.erp.dto.BudgetFilterRequest;
import com.example.erp.dto.BudgetResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpsertBudgetRequest;

public interface BudgetService {

    PageResponse<BudgetResponse> listBudgets(BudgetFilterRequest filter);

    BudgetResponse upsertBudget(UpsertBudgetRequest request, String actingUsername);

    void deleteBudget(Long id);
}
