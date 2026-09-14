package com.example.erp.service;

import com.example.erp.dto.ApDetailResponse;
import com.example.erp.dto.ApPaymentResponse;
import com.example.erp.dto.ApReportFilterRequest;
import com.example.erp.dto.ApSummaryResponse;
import com.example.erp.dto.SupplierBalanceResponse;
import com.example.erp.dto.SupplierStatementResponse;

// All computed fresh from PurchaseInvoice/PurchaseInvoiceLine/
// PurchaseCreditNote/SupplierPaymentAllocation — the same authoritative
// source PurchaseInvoiceServiceImpl.agingReport() already uses — rather than
// trusting Supplier.currentBalance, which is a maintained cache that could
// in principle drift. Mirrors ArReportService exactly, for the payables side.
public interface ApReportService {

    ApSummaryResponse summary(ApReportFilterRequest filter);

    ApDetailResponse detail(ApReportFilterRequest filter);

    SupplierBalanceResponse supplierBalance(ApReportFilterRequest filter);

    SupplierStatementResponse supplierStatement(ApReportFilterRequest filter);

    ApPaymentResponse payments(ApReportFilterRequest filter);
}
