package com.example.erp.repository;

import com.example.erp.entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long>, JpaSpecificationExecutor<WorkOrder> {
    List<WorkOrder> findByManufacturingOrderIdOrderBySequenceNumberAsc(Long manufacturingOrderId);
    void deleteByManufacturingOrderId(Long manufacturingOrderId);
    boolean existsByRoutingOperationIdIn(List<Long> routingOperationIds);
}
