package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UnitOfMeasureFilterRequest;
import com.example.erp.dto.UnitOfMeasureRequest;
import com.example.erp.dto.UnitOfMeasureResponse;

public interface UnitOfMeasureService {

    PageResponse<UnitOfMeasureResponse> list(UnitOfMeasureFilterRequest filter);

    UnitOfMeasureResponse get(Long id);

    UnitOfMeasureResponse create(UnitOfMeasureRequest request);

    UnitOfMeasureResponse update(Long id, UnitOfMeasureRequest request);

    void delete(Long id);
}
