package com.example.erp.repository;

import com.example.erp.entity.FiscalYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface FiscalYearRepository extends JpaRepository<FiscalYear, Long>, JpaSpecificationExecutor<FiscalYear> {
    List<FiscalYear> findByCompanyId(Long companyId);
    boolean existsByCompanyIdAndName(Long companyId, String name);
}
