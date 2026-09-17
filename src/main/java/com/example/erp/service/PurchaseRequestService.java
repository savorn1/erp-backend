package com.example.erp.service;

import com.example.erp.dto.CreatePurchaseRequestRequest;
import com.example.erp.dto.GenerateFromLowStockRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PurchaseRequestFilterRequest;
import com.example.erp.dto.PurchaseRequestResponse;
import com.example.erp.dto.RejectPurchaseRequestRequest;
import com.example.erp.dto.UpdatePurchaseRequestRequest;

public interface PurchaseRequestService {

    PageResponse<PurchaseRequestResponse> listPurchaseRequests(PurchaseRequestFilterRequest filter);

    PurchaseRequestResponse getPurchaseRequest(Long id);

    PurchaseRequestResponse createPurchaseRequest(CreatePurchaseRequestRequest request, String actingUsername);

    // Builds one DRAFT PurchaseRequest from whatever the Low Stock report
    // currently considers low (same "available < reorderPoint" definition,
    // see InventoryReportServiceImpl.lowStock) — one line per product,
    // shortfall summed across every warehouse it's low in.
    PurchaseRequestResponse generateFromLowStock(GenerateFromLowStockRequest request, String actingUsername);

    PurchaseRequestResponse updatePurchaseRequest(Long id, UpdatePurchaseRequestRequest request);

    PurchaseRequestResponse submitPurchaseRequest(Long id);

    PurchaseRequestResponse approvePurchaseRequest(Long id);

    PurchaseRequestResponse rejectPurchaseRequest(Long id, RejectPurchaseRequestRequest request);

    void deletePurchaseRequest(Long id);
}
