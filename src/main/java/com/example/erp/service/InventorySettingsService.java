package com.example.erp.service;

import com.example.erp.dto.InventorySettingsRequest;
import com.example.erp.dto.InventorySettingsResponse;
import com.example.erp.entity.InventorySettings;

public interface InventorySettingsService {

    InventorySettingsResponse getForCompany(Long companyId);

    InventorySettingsResponse upsert(InventorySettingsRequest request);

    // Internal use by StockAvailabilityService and friends — returns the
    // entity with the standard defaults substituted when the company hasn't
    // saved any row yet, rather than the DTO-shaped public response.
    InventorySettings resolveForCompany(Long companyId);
}
