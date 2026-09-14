package com.example.erp.service;

import com.example.erp.dto.CompleteManufacturingOrderRequest;
import com.example.erp.dto.CreateManufacturingOrderRequest;
import com.example.erp.dto.ManufacturingOrderFilterRequest;
import com.example.erp.dto.ManufacturingOrderResponse;
import com.example.erp.dto.MaterialAvailabilityRowResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.QualityCheckRequest;
import com.example.erp.dto.UpdateManufacturingOrderRequest;

import java.util.List;

public interface ManufacturingOrderService {

    PageResponse<ManufacturingOrderResponse> listOrders(ManufacturingOrderFilterRequest filter);

    ManufacturingOrderResponse getOrder(Long id);

    ManufacturingOrderResponse createOrder(CreateManufacturingOrderRequest request, String actingUsername);

    ManufacturingOrderResponse updateOrder(Long id, UpdateManufacturingOrderRequest request);

    ManufacturingOrderResponse releaseOrder(Long id);

    ManufacturingOrderResponse startProduction(Long id, String actingUsername);

    ManufacturingOrderResponse completeProduction(Long id, CompleteManufacturingOrderRequest request, String actingUsername);

    ManufacturingOrderResponse recordQualityCheck(Long id, QualityCheckRequest request, String actingUsername);

    ManufacturingOrderResponse cancelOrder(Long id);

    void deleteOrder(Long id);

    List<MaterialAvailabilityRowResponse> checkMaterialAvailability(Long id);
}
