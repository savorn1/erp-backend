package com.example.erp.repository;

import com.example.erp.entity.PaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, Long> {

    List<PaymentAllocation> findByPaymentId(Long paymentId);

    List<PaymentAllocation> findByInvoiceId(Long invoiceId);

    List<PaymentAllocation> findByInvoiceIdIn(List<Long> invoiceIds);
}
