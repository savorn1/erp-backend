package com.example.erp.service;

import com.example.erp.dto.CustomerTypeFilterRequest;
import com.example.erp.dto.CustomerTypeRequest;
import com.example.erp.dto.CustomerTypeResponse;
import com.example.erp.dto.PageResponse;

public interface CustomerTypeService {

    PageResponse<CustomerTypeResponse> list(CustomerTypeFilterRequest filter);

    CustomerTypeResponse get(Long id);

    CustomerTypeResponse create(CustomerTypeRequest request);

    CustomerTypeResponse update(Long id, CustomerTypeRequest request);

    void delete(Long id);
}
