package com.example.erp.service;

import com.example.erp.dto.CreatePurchaseOrderRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PurchaseOrderFilterRequest;
import com.example.erp.dto.PurchaseOrderResponse;
import com.example.erp.dto.SendDocumentEmailRequest;
import com.example.erp.dto.UpdatePurchaseOrderRequest;

public interface PurchaseOrderService {

    PageResponse<PurchaseOrderResponse> listPurchaseOrders(PurchaseOrderFilterRequest filter);

    PurchaseOrderResponse getPurchaseOrder(Long id);

    PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request, String actingUsername);

    PurchaseOrderResponse updatePurchaseOrder(Long id, UpdatePurchaseOrderRequest request);

    PurchaseOrderResponse submitPurchaseOrder(Long id);

    PurchaseOrderResponse approvePurchaseOrder(Long id, String actingUsername);

    PurchaseOrderResponse sendPurchaseOrder(Long id);

    PurchaseOrderResponse cancelPurchaseOrder(Long id);

    void deletePurchaseOrder(Long id);

    void emailPurchaseOrder(Long id, SendDocumentEmailRequest request);
}
