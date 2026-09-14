package com.example.erp.repository;

import com.example.erp.entity.SupplierPaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierPaymentAllocationRepository extends JpaRepository<SupplierPaymentAllocation, Long> {

    List<SupplierPaymentAllocation> findBySupplierPaymentId(Long supplierPaymentId);

    List<SupplierPaymentAllocation> findByPurchaseInvoiceId(Long purchaseInvoiceId);

    List<SupplierPaymentAllocation> findByPurchaseInvoiceIdIn(List<Long> purchaseInvoiceIds);
}
