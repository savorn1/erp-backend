package com.example.erp.repository;

import com.example.erp.entity.PurchaseOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, Long> {

    List<PurchaseOrderLine> findByPurchaseOrderId(Long purchaseOrderId);

    void deleteByPurchaseOrderId(Long purchaseOrderId);
}
