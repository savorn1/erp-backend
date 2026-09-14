package com.example.erp.repository;

import com.example.erp.entity.RoutingOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutingOperationRepository extends JpaRepository<RoutingOperation, Long> {
    List<RoutingOperation> findByRoutingIdOrderBySequenceNumberAsc(Long routingId);
    void deleteByRoutingId(Long routingId);
    boolean existsByWorkCenterId(Long workCenterId);
    boolean existsByMachineId(Long machineId);
}
