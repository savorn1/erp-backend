package com.example.erp.service;

import com.example.erp.dto.CreatePaymentRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PaymentFilterRequest;
import com.example.erp.dto.PaymentResponse;
import com.example.erp.dto.RefundPaymentRequest;

public interface PaymentService {

    PageResponse<PaymentResponse> listPayments(PaymentFilterRequest filter);

    PaymentResponse getPayment(Long id);

    PaymentResponse recordPayment(CreatePaymentRequest request, String actingUsername);

    PaymentResponse refundPayment(Long id, RefundPaymentRequest request, String actingUsername);
}
