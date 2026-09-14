package com.example.erp.repository;

import com.example.erp.entity.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CostCenterRepository extends JpaRepository<CostCenter, Long>, JpaSpecificationExecutor<CostCenter> {
    boolean existsByCompanyIdAndCode(Long companyId, String code);
    boolean existsByCompanyIdAndCodeAndIdNot(Long companyId, String code, Long id);
}
