package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PosExchangeFilterRequest;
import com.example.erp.dto.PosExchangeRequest;
import com.example.erp.dto.PosExchangeResponse;

public interface PosExchangeService {

    PageResponse<PosExchangeResponse> list(PosExchangeFilterRequest filter);

    PosExchangeResponse get(Long id);

    PosExchangeResponse createExchange(PosExchangeRequest request, String actingUsername);
}
