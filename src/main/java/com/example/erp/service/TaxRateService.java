package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.TaxRateFilterRequest;
import com.example.erp.dto.TaxRateRequest;
import com.example.erp.dto.TaxRateResponse;

public interface TaxRateService {

    PageResponse<TaxRateResponse> list(TaxRateFilterRequest filter);

    TaxRateResponse get(Long id);

    TaxRateResponse create(TaxRateRequest request);

    TaxRateResponse update(Long id, TaxRateRequest request);

    void delete(Long id);
}
