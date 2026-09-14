package com.example.erp.service;

import com.example.erp.dto.ArBadDebtResponse;
import com.example.erp.dto.ArCollectionResponse;
import com.example.erp.dto.ArDetailResponse;
import com.example.erp.dto.ArReportFilterRequest;
import com.example.erp.dto.ArSummaryResponse;
import com.example.erp.dto.CustomerBalanceResponse;
import com.example.erp.dto.CustomerStatementResponse;

// All computed fresh from Invoice/InvoiceLine/CreditNote/PaymentAllocation —
// the same authoritative source InvoiceServiceImpl.agingReport() already
// uses — rather than trusting Customer.currentBalance, which is a
// maintained cache that could in principle drift.
public interface ArReportService {

    ArSummaryResponse summary(ArReportFilterRequest filter);

    ArDetailResponse detail(ArReportFilterRequest filter);

    CustomerBalanceResponse customerBalance(ArReportFilterRequest filter);

    CustomerStatementResponse customerStatement(ArReportFilterRequest filter);

    ArCollectionResponse collections(ArReportFilterRequest filter);

    ArBadDebtResponse badDebt(ArReportFilterRequest filter);
}
