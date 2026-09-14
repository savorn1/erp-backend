package com.example.erp.service;

import com.example.erp.dto.PriceLookupResponse;

public interface PricingService {

    PriceLookupResponse lookup(Long companyId, Long customerId, Long productId);
}
