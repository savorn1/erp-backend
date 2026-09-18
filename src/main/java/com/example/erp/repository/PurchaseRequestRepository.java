package com.example.erp.repository;

import com.example.erp.entity.PurchaseRequest;
import com.example.erp.entity.PurchaseRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long>, JpaSpecificationExecutor<PurchaseRequest> {

    long countByStatus(PurchaseRequestStatus status);
}
