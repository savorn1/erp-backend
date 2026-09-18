package com.example.erp.service;

import com.example.erp.dto.CommissionEntryFilterRequest;
import com.example.erp.dto.CommissionEntryResponse;
import com.example.erp.dto.CommissionReportFilterRequest;
import com.example.erp.dto.CommissionReportResponse;
import com.example.erp.dto.MarkCommissionPaidRequest;
import com.example.erp.dto.PageResponse;

public interface CommissionService {

    CommissionReportResponse report(CommissionReportFilterRequest filter);

    PageResponse<CommissionEntryResponse> listEntries(CommissionEntryFilterRequest filter);

    void markPaid(MarkCommissionPaidRequest request);
}
