package com.example.erp.repository;

import com.example.erp.entity.SupplierPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SupplierPaymentRepository extends JpaRepository<SupplierPayment, Long>, JpaSpecificationExecutor<SupplierPayment> {

    List<SupplierPayment> findByRelatedPaymentId(Long relatedPaymentId);
}
