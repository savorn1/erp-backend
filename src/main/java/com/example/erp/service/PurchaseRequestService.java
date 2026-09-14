package com.example.erp.service;

import com.example.erp.dto.CreatePurchaseRequestRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PurchaseRequestFilterRequest;
import com.example.erp.dto.PurchaseRequestResponse;
import com.example.erp.dto.RejectPurchaseRequestRequest;
import com.example.erp.dto.UpdatePurchaseRequestRequest;

public interface PurchaseRequestService {

    PageResponse<PurchaseRequestResponse> listPurchaseRequests(PurchaseRequestFilterRequest filter);

    PurchaseRequestResponse getPurchaseRequest(Long id);

    PurchaseRequestResponse createPurchaseRequest(CreatePurchaseRequestRequest request, String actingUsername);

    PurchaseRequestResponse updatePurchaseRequest(Long id, UpdatePurchaseRequestRequest request);

    PurchaseRequestResponse submitPurchaseRequest(Long id);

    PurchaseRequestResponse approvePurchaseRequest(Long id);

    PurchaseRequestResponse rejectPurchaseRequest(Long id, RejectPurchaseRequestRequest request);

    void deletePurchaseRequest(Long id);
}
