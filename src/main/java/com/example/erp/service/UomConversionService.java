package com.example.erp.service;

import com.example.erp.dto.ConvertUnitsRequest;
import com.example.erp.dto.ConvertUnitsResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UomConversionFilterRequest;
import com.example.erp.dto.UomConversionRequest;
import com.example.erp.dto.UomConversionResponse;

public interface UomConversionService {

    PageResponse<UomConversionResponse> list(UomConversionFilterRequest filter);

    UomConversionResponse get(Long id);

    UomConversionResponse create(UomConversionRequest request);

    UomConversionResponse update(Long id, UomConversionRequest request);

    void delete(Long id);

    // Generic, product-independent unit conversion — tries a direct edge,
    // then its reverse, then a path through the shared category's base unit.
    ConvertUnitsResponse convert(ConvertUnitsRequest request);
}
