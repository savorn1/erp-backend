package com.example.erp.repository;

import com.example.erp.entity.AccountingPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AccountingPeriodRepository extends JpaRepository<AccountingPeriod, Long>, JpaSpecificationExecutor<AccountingPeriod> {
    List<AccountingPeriod> findByFiscalYearIdOrderByPeriodNumberAsc(Long fiscalYearId);
    List<AccountingPeriod> findByCompanyId(Long companyId);
    void deleteByFiscalYearId(Long fiscalYearId);

    // The period (if any) covering a given posting date for a company — used
    // by JournalEntryServiceImpl/AutoPostingServiceImpl to check the lock.
    Optional<AccountingPeriod> findByCompanyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long companyId, LocalDate onOrAfterStart, LocalDate onOrBeforeEnd);
}
