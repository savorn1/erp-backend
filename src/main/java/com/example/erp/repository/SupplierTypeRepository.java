package com.example.erp.repository;

import com.example.erp.entity.SupplierType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SupplierTypeRepository extends JpaRepository<SupplierType, Long>, JpaSpecificationExecutor<SupplierType> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Optional<SupplierType> findByNameIgnoreCase(String name);
}
