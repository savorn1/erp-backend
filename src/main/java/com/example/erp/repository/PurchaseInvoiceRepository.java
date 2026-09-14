package com.example.erp.repository;

import com.example.erp.entity.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long>, JpaSpecificationExecutor<PurchaseInvoice> {
}
