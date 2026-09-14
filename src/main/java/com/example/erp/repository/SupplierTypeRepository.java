package com.example.erp.repository;

import com.example.erp.entity.SupplierType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SupplierTypeRepository extends JpaRepository<SupplierType, Long>, JpaSpecificationExecutor<SupplierType> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
