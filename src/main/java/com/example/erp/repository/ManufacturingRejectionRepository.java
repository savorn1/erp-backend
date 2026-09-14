package com.example.erp.repository;

import com.example.erp.entity.ManufacturingRejection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ManufacturingRejectionRepository extends JpaRepository<ManufacturingRejection, Long>, JpaSpecificationExecutor<ManufacturingRejection> {
    List<ManufacturingRejection> findByManufacturingOrderId(Long manufacturingOrderId);
}
