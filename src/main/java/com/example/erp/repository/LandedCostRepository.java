package com.example.erp.repository;

import com.example.erp.entity.LandedCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LandedCostRepository extends JpaRepository<LandedCost, Long>, JpaSpecificationExecutor<LandedCost> {
}
