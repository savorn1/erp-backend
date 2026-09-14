package com.example.erp.service;

import com.example.erp.dto.TaxByCustomerResponse;
import com.example.erp.dto.TaxByProductResponse;
import com.example.erp.dto.TaxBySupplierResponse;
import com.example.erp.dto.TaxDetailResponse;
import com.example.erp.dto.TaxReportFilterRequest;
import com.example.erp.dto.TaxReportResponse;

public interface TaxReportService {

    TaxReportResponse generate(TaxReportFilterRequest filter);

    TaxDetailResponse detail(TaxReportFilterRequest filter);

    TaxByCustomerResponse byCustomer(TaxReportFilterRequest filter);

    TaxBySupplierResponse bySupplier(TaxReportFilterRequest filter);

    TaxByProductResponse byProduct(TaxReportFilterRequest filter);
}
