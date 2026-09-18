package com.example.erp.repository;

import com.example.erp.entity.InvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, Long> {

    List<InvoiceLine> findByInvoiceId(Long invoiceId);

    List<InvoiceLine> findByInvoiceIdIn(List<Long> invoiceIds);

    void deleteByInvoiceId(Long invoiceId);
}
