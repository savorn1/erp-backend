package com.example.erp.service;

import com.example.erp.dto.CreateLandedCostRequest;
import com.example.erp.dto.LandedCostFilterRequest;
import com.example.erp.dto.LandedCostResponse;
import com.example.erp.dto.PageResponse;

public interface LandedCostService {

    PageResponse<LandedCostResponse> listLandedCosts(LandedCostFilterRequest filter);

    LandedCostResponse getLandedCost(Long id);

    LandedCostResponse createLandedCost(CreateLandedCostRequest request, String actingUsername);
}
