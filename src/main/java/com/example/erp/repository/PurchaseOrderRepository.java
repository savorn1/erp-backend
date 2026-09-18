package com.example.erp.repository;

import com.example.erp.entity.PurchaseOrder;
import com.example.erp.entity.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {

    long countByStatus(PurchaseOrderStatus status);
}
