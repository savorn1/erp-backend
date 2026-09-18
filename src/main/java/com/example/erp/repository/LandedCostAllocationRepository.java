package com.example.erp.repository;

import com.example.erp.entity.LandedCostAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LandedCostAllocationRepository extends JpaRepository<LandedCostAllocation, Long> {

    List<LandedCostAllocation> findByLandedCostId(Long landedCostId);
}
