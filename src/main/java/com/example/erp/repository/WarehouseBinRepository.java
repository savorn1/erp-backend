package com.example.erp.repository;

import com.example.erp.entity.WarehouseBin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WarehouseBinRepository extends JpaRepository<WarehouseBin, Long>, JpaSpecificationExecutor<WarehouseBin> {

    boolean existsByZoneIdAndName(Long zoneId, String name);

    boolean existsByZoneIdAndNameAndIdNot(Long zoneId, String name, Long id);

    boolean existsByZoneId(Long zoneId);

    long countByZoneId(Long zoneId);
}
