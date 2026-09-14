package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.SupplierTypeFilterRequest;
import com.example.erp.dto.SupplierTypeRequest;
import com.example.erp.dto.SupplierTypeResponse;

public interface SupplierTypeService {

    PageResponse<SupplierTypeResponse> list(SupplierTypeFilterRequest filter);

    SupplierTypeResponse get(Long id);

    SupplierTypeResponse create(SupplierTypeRequest request);

    SupplierTypeResponse update(Long id, SupplierTypeRequest request);

    void delete(Long id);
}
