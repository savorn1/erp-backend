package com.example.erp.service;

import com.example.erp.dto.CreateProductionPlanRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductionPlanFilterRequest;
import com.example.erp.dto.ProductionPlanResponse;
import com.example.erp.dto.UpdateProductionPlanRequest;

public interface ProductionPlanService {

    PageResponse<ProductionPlanResponse> listPlans(ProductionPlanFilterRequest filter);

    ProductionPlanResponse getPlan(Long id);

    ProductionPlanResponse createPlan(CreateProductionPlanRequest request, String actingUsername);

    ProductionPlanResponse updatePlan(Long id, UpdateProductionPlanRequest request);

    ProductionPlanResponse activatePlan(Long id);

    ProductionPlanResponse closePlan(Long id);

    void deletePlan(Long id);
}
