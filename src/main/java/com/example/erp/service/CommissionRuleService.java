package com.example.erp.service;

import com.example.erp.dto.CommissionRuleFilterRequest;
import com.example.erp.dto.CommissionRuleRequest;
import com.example.erp.dto.CommissionRuleResponse;
import com.example.erp.dto.PageResponse;

public interface CommissionRuleService {

    PageResponse<CommissionRuleResponse> list(CommissionRuleFilterRequest filter);

    CommissionRuleResponse get(Long id);

    CommissionRuleResponse create(CommissionRuleRequest request);

    CommissionRuleResponse update(Long id, CommissionRuleRequest request);

    void delete(Long id);
}
