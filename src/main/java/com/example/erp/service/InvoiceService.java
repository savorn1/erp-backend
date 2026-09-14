package com.example.erp.service;

import com.example.erp.dto.CreateInvoiceRequest;
import com.example.erp.dto.InvoiceAgingFilterRequest;
import com.example.erp.dto.InvoiceAgingReportResponse;
import com.example.erp.dto.InvoiceFilterRequest;
import com.example.erp.dto.InvoiceResponse;
import com.example.erp.dto.PageResponse;

public interface InvoiceService {

    PageResponse<InvoiceResponse> listInvoices(InvoiceFilterRequest filter);

    // Every approved invoice's outstanding balance, bucketed by customer and
    // by how many days past due (current / 1-30 / 31-60 / 61-90 / 90+).
    InvoiceAgingReportResponse agingReport(InvoiceAgingFilterRequest filter);

    InvoiceResponse getInvoice(Long id);

    InvoiceResponse createFromSalesOrder(Long salesOrderId, CreateInvoiceRequest request, String actingUsername);

    InvoiceResponse createFromDelivery(Long deliveryId, CreateInvoiceRequest request, String actingUsername);

    InvoiceResponse approveInvoice(Long id, String actingUsername);

    InvoiceResponse cancelInvoice(Long id, String actingUsername);

    void deleteInvoice(Long id);
}
