package com.example.erp.repository;

import com.example.erp.entity.ProductionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, Long>, JpaSpecificationExecutor<ProductionPlan> {
}
