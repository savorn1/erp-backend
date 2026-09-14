package com.example.erp.repository;

import com.example.erp.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long>, JpaSpecificationExecutor<Warehouse> {

    boolean existsByCompanyIdAndName(Long companyId, String name);

    boolean existsByCompanyIdAndNameAndIdNot(Long companyId, String name, Long id);
}
