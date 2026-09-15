package com.example.erp.service;

import com.example.erp.dto.PostingRuleRequest;
import com.example.erp.dto.PostingRuleResponse;

public interface PostingRuleService {

    // Returns an all-nulls response (no id) if the company has never
    // configured one — not a 404, since "not configured yet" is the normal
    // starting state, not an error.
    PostingRuleResponse getForCompany(Long companyId);

    // Creates the company's PostingRule row if it doesn't exist yet,
    // otherwise replaces every field on the existing one.
    PostingRuleResponse upsert(PostingRuleRequest request);

    // Seeds the company's standard chart of accounts (see
    // AccountService.seedSampleChartOfAccounts, idempotent) if needed, then
    // fills in every currently-unmapped field from it, one default account
    // per module (Sales, Purchasing, Cash & bank) — any field the company
    // has already mapped is left untouched.
    PostingRuleResponse seedFromChartOfAccounts(Long companyId);
}
