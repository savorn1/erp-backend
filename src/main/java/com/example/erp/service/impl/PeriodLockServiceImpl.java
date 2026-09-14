package com.example.erp.service.impl;

import com.example.erp.entity.AccountingPeriodStatus;
import com.example.erp.repository.AccountingPeriodRepository;
import com.example.erp.service.PeriodLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PeriodLockServiceImpl implements PeriodLockService {

    private final AccountingPeriodRepository accountingPeriodRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isLocked(Long companyId, LocalDate date) {
        return accountingPeriodRepository
                .findByCompanyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(companyId, date, date)
                .map(period -> period.getStatus() == AccountingPeriodStatus.CLOSED)
                .orElse(false);
    }
}
