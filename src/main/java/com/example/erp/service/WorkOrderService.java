package com.example.erp.service;

import com.example.erp.dto.CompleteWorkOrderRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.WorkOrderFilterRequest;
import com.example.erp.dto.WorkOrderResponse;

public interface WorkOrderService {

    PageResponse<WorkOrderResponse> listWorkOrders(WorkOrderFilterRequest filter);

    WorkOrderResponse getWorkOrder(Long id);

    WorkOrderResponse start(Long id);

    WorkOrderResponse complete(Long id, CompleteWorkOrderRequest request);
}
