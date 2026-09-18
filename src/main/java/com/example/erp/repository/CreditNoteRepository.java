package com.example.erp.repository;

import com.example.erp.entity.CreditNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CreditNoteRepository extends JpaRepository<CreditNote, Long>, JpaSpecificationExecutor<CreditNote> {

    List<CreditNote> findByInvoiceId(Long invoiceId);

    List<CreditNote> findByInvoiceIdIn(List<Long> invoiceIds);
}
