package com.example.erp.repository;

import com.example.erp.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SupplierRepository extends JpaRepository<Supplier, Long>, JpaSpecificationExecutor<Supplier> {

    boolean existsByCompanyIdAndName(Long companyId, String name);

    boolean existsByCompanyIdAndNameAndIdNot(Long companyId, String name, Long id);
}
