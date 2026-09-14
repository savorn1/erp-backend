package com.example.erp.entity;

public enum PurchaseRequestStatus {
    DRAFT,
    // Submitted for approval — see PurchaseRequestServiceImpl.approvePurchaseRequest/rejectPurchaseRequest.
    SUBMITTED,
    APPROVED,
    REJECTED
}
