package com.example.erp.service;

import com.example.erp.dto.CostCenterFilterRequest;
import com.example.erp.dto.CostCenterRequest;
import com.example.erp.dto.CostCenterResponse;
import com.example.erp.dto.PageResponse;

public interface CostCenterService {

    PageResponse<CostCenterResponse> list(CostCenterFilterRequest filter);

    CostCenterResponse get(Long id);

    CostCenterResponse create(CostCenterRequest request);

    CostCenterResponse update(Long id, CostCenterRequest request);

    void delete(Long id);
}
