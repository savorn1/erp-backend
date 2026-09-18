package com.example.erp.repository;

import com.example.erp.entity.StockAdjustment;
import com.example.erp.entity.StockAdjustmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long>, JpaSpecificationExecutor<StockAdjustment> {

    long countByStatus(StockAdjustmentStatus status);
}
