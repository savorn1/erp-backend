package com.example.erp.repository;

import com.example.erp.entity.PurchaseCreditNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PurchaseCreditNoteRepository extends JpaRepository<PurchaseCreditNote, Long>, JpaSpecificationExecutor<PurchaseCreditNote> {

    List<PurchaseCreditNote> findByPurchaseInvoiceId(Long purchaseInvoiceId);
}
