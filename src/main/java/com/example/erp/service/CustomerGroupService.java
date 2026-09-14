package com.example.erp.service;

import com.example.erp.dto.CustomerGroupFilterRequest;
import com.example.erp.dto.CustomerGroupRequest;
import com.example.erp.dto.CustomerGroupResponse;
import com.example.erp.dto.PageResponse;

public interface CustomerGroupService {

    PageResponse<CustomerGroupResponse> list(CustomerGroupFilterRequest filter);

    CustomerGroupResponse get(Long id);

    CustomerGroupResponse create(CustomerGroupRequest request);

    CustomerGroupResponse update(Long id, CustomerGroupRequest request);

    void delete(Long id);
}
