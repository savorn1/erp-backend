package com.example.erp.service;

import com.example.erp.dto.InventoryOverviewFilterRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.InventoryOverviewResponse;

public interface InventoryOverviewService {

    PageResponse<InventoryOverviewResponse> getOverview(InventoryOverviewFilterRequest filter);
}
