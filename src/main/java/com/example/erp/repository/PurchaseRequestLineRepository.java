package com.example.erp.repository;

import com.example.erp.entity.PurchaseRequestLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRequestLineRepository extends JpaRepository<PurchaseRequestLine, Long> {

    List<PurchaseRequestLine> findByPurchaseRequestId(Long purchaseRequestId);

    void deleteByPurchaseRequestId(Long purchaseRequestId);
}
