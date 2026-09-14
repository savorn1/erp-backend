package com.example.erp.repository;

import com.example.erp.entity.ManufacturingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ManufacturingOrderRepository extends JpaRepository<ManufacturingOrder, Long>, JpaSpecificationExecutor<ManufacturingOrder> {
}
