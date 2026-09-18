package com.example.erp.repository;

import com.example.erp.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long>, JpaSpecificationExecutor<Budget> {

    Optional<Budget> findByCompanyIdAndAccountIdAndCostCenterIdAndAccountingPeriodId(
            Long companyId, Long accountId, Long costCenterId, Long accountingPeriodId);

    List<Budget> findByAccountingPeriodIdIn(List<Long> accountingPeriodIds);
}
