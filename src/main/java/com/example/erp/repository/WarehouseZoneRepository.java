package com.example.erp.repository;

import com.example.erp.entity.WarehouseZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WarehouseZoneRepository extends JpaRepository<WarehouseZone, Long>, JpaSpecificationExecutor<WarehouseZone> {

    boolean existsByWarehouseIdAndName(Long warehouseId, String name);

    boolean existsByWarehouseIdAndNameAndIdNot(Long warehouseId, String name, Long id);

    boolean existsByWarehouseId(Long warehouseId);

    long countByWarehouseId(Long warehouseId);
}
