package com.example.erp.service;

import com.example.erp.dto.CollectionByCustomerResponse;
import com.example.erp.dto.CollectionBySalespersonResponse;
import com.example.erp.dto.PaymentByBranchResponse;
import com.example.erp.dto.PaymentByMethodResponse;
import com.example.erp.dto.PaymentDetailResponse;
import com.example.erp.dto.PaymentReconciliationResponse;
import com.example.erp.dto.PaymentReportFilterRequest;
import com.example.erp.dto.PaymentSummaryResponse;
import com.example.erp.dto.RefundReportResponse;
import com.example.erp.dto.TransferReportResponse;

// Cross-ledger payment reporting over Payment (customer), SupplierPayment,
// and BankTransaction. Per-ledger views already exist elsewhere — AR
// "collections" (ArReportService), AP "payments" (ApReportService) and the
// AR/AP aging reports — so this service only adds the combined and
// differently-sliced cuts.
public interface PaymentReportService {

    PaymentSummaryResponse summary(PaymentReportFilterRequest filter);

    PaymentDetailResponse detail(PaymentReportFilterRequest filter);

    PaymentByMethodResponse byMethod(PaymentReportFilterRequest filter);

    PaymentByBranchResponse byBranch(PaymentReportFilterRequest filter);

    RefundReportResponse refunds(PaymentReportFilterRequest filter);

    TransferReportResponse transfers(PaymentReportFilterRequest filter);

    CollectionByCustomerResponse collectionByCustomer(PaymentReportFilterRequest filter);

    CollectionBySalespersonResponse collectionBySalesperson(PaymentReportFilterRequest filter);

    PaymentReconciliationResponse reconciliation(PaymentReportFilterRequest filter);
}
