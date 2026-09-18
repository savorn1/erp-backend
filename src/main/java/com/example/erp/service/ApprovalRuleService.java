package com.example.erp.service;

import com.example.erp.dto.ApprovalRuleFilterRequest;
import com.example.erp.dto.ApprovalRuleRequest;
import com.example.erp.dto.ApprovalRuleResponse;
import com.example.erp.dto.PageResponse;

public interface ApprovalRuleService {

    PageResponse<ApprovalRuleResponse> listRules(ApprovalRuleFilterRequest filter);

    ApprovalRuleResponse createRule(ApprovalRuleRequest request);

    ApprovalRuleResponse updateRule(Long id, ApprovalRuleRequest request);

    void deleteRule(Long id);
}
