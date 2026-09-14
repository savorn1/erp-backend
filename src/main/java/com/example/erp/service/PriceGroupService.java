package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PriceGroupFilterRequest;
import com.example.erp.dto.PriceGroupRequest;
import com.example.erp.dto.PriceGroupResponse;

public interface PriceGroupService {

    PageResponse<PriceGroupResponse> list(PriceGroupFilterRequest filter);

    PriceGroupResponse get(Long id);

    PriceGroupResponse create(PriceGroupRequest request);

    PriceGroupResponse update(Long id, PriceGroupRequest request);

    void delete(Long id);
}
