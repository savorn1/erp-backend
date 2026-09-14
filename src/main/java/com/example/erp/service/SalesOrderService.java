package com.example.erp.service;

import com.example.erp.dto.CreateSalesOrderRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.SalesOrderFilterRequest;
import com.example.erp.dto.SalesOrderResponse;
import com.example.erp.dto.UpdateSalesOrderRequest;

public interface SalesOrderService {

    PageResponse<SalesOrderResponse> listSalesOrders(SalesOrderFilterRequest filter);

    SalesOrderResponse getSalesOrder(Long id);

    SalesOrderResponse createSalesOrder(CreateSalesOrderRequest request, String actingUsername);

    SalesOrderResponse updateSalesOrder(Long id, UpdateSalesOrderRequest request);

    SalesOrderResponse submitSalesOrder(Long id);

    SalesOrderResponse approveSalesOrder(Long id, String actingUsername);

    SalesOrderResponse cancelSalesOrder(Long id);

    void deleteSalesOrder(Long id);
}
