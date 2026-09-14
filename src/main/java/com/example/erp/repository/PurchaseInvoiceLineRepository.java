package com.example.erp.repository;

import com.example.erp.entity.PurchaseInvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseInvoiceLineRepository extends JpaRepository<PurchaseInvoiceLine, Long> {

    List<PurchaseInvoiceLine> findByPurchaseInvoiceId(Long purchaseInvoiceId);

    void deleteByPurchaseInvoiceId(Long purchaseInvoiceId);
}
