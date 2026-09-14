package com.example.erp.service;

import com.example.erp.dto.CreatePurchaseInvoiceRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PurchaseInvoiceAgingFilterRequest;
import com.example.erp.dto.PurchaseInvoiceAgingReportResponse;
import com.example.erp.dto.PurchaseInvoiceFilterRequest;
import com.example.erp.dto.PurchaseInvoiceResponse;

public interface PurchaseInvoiceService {

    PageResponse<PurchaseInvoiceResponse> listPurchaseInvoices(PurchaseInvoiceFilterRequest filter);

    // Every approved purchase invoice's outstanding balance, bucketed by
    // supplier and by how many days past due (current / 1-30 / 31-60 / 61-90 / 90+).
    PurchaseInvoiceAgingReportResponse agingReport(PurchaseInvoiceAgingFilterRequest filter);

    PurchaseInvoiceResponse getPurchaseInvoice(Long id);

    // Bills the full ordered quantity of a sent (or already-receiving) PO —
    // a 2-way match. See PurchaseInvoiceServiceImpl.
    PurchaseInvoiceResponse createFromPurchaseOrder(Long purchaseOrderId, CreatePurchaseInvoiceRequest request, String actingUsername);

    // Bills only the quantities that passed quality check on one specific
    // receipt (a 3-way match: PO + goods receipt) — the receipt must be fully
    // quality-checked (COMPLETED) first.
    PurchaseInvoiceResponse createFromGoodsReceipt(Long goodsReceiptId, CreatePurchaseInvoiceRequest request, String actingUsername);

    PurchaseInvoiceResponse approvePurchaseInvoice(Long id, String actingUsername);

    PurchaseInvoiceResponse cancelPurchaseInvoice(Long id, String actingUsername);

    void deletePurchaseInvoice(Long id);
}
