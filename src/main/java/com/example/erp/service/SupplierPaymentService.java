package com.example.erp.service;

import com.example.erp.dto.CreateSupplierPaymentRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RefundSupplierPaymentRequest;
import com.example.erp.dto.SupplierPaymentFilterRequest;
import com.example.erp.dto.SupplierPaymentResponse;

public interface SupplierPaymentService {

    PageResponse<SupplierPaymentResponse> listPayments(SupplierPaymentFilterRequest filter);

    SupplierPaymentResponse getPayment(Long id);

    SupplierPaymentResponse recordPayment(CreateSupplierPaymentRequest request, String actingUsername);

    SupplierPaymentResponse refundPayment(Long id, RefundSupplierPaymentRequest request, String actingUsername);
}
