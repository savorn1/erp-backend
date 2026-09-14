package com.example.erp.repository;

import com.example.erp.entity.TaxRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaxRateRepository extends JpaRepository<TaxRate, Long>, JpaSpecificationExecutor<TaxRate> {

    boolean existsByCompanyIdAndCode(Long companyId, String code);

    boolean existsByCompanyIdAndCodeAndIdNot(Long companyId, String code, Long id);
}
