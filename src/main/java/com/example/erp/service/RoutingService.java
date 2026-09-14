package com.example.erp.service;

import com.example.erp.dto.CreateRoutingRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RoutingFilterRequest;
import com.example.erp.dto.RoutingResponse;
import com.example.erp.dto.UpdateRoutingRequest;

public interface RoutingService {

    PageResponse<RoutingResponse> listRoutings(RoutingFilterRequest filter);

    RoutingResponse getRouting(Long id);

    RoutingResponse createRouting(CreateRoutingRequest request, String actingUsername);

    RoutingResponse updateRouting(Long id, UpdateRoutingRequest request);

    RoutingResponse activateRouting(Long id);

    RoutingResponse deactivateRouting(Long id);

    void deleteRouting(Long id);
}
