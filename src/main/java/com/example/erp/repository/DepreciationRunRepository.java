package com.example.erp.repository;

import com.example.erp.entity.DepreciationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DepreciationRunRepository extends JpaRepository<DepreciationRun, Long>, JpaSpecificationExecutor<DepreciationRun> {
    Optional<DepreciationRun> findByCompanyIdAndAccountingPeriodId(Long companyId, Long accountingPeriodId);
}
