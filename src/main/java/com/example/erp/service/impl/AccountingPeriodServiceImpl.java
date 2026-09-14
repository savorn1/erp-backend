package com.example.erp.service.impl;

import com.example.erp.dto.AccountingPeriodFilterRequest;
import com.example.erp.dto.AccountingPeriodResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.AccountingPeriod;
import com.example.erp.entity.AccountingPeriodStatus;
import com.example.erp.entity.FiscalYear;
import com.example.erp.entity.FiscalYearStatus;
import com.example.erp.exception.AppException;
import com.example.erp.repository.AccountingPeriodRepository;
import com.example.erp.repository.FiscalYearRepository;
import com.example.erp.service.AccountingPeriodService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountingPeriodServiceImpl implements AccountingPeriodService {

    private final AccountingPeriodRepository accountingPeriodRepository;
    private final FiscalYearRepository fiscalYearRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AccountingPeriodResponse> list(AccountingPeriodFilterRequest filter) {
        List<Specification<AccountingPeriod>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getFiscalYearId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("fiscalYearId"), filter.getFiscalYearId()));
        if (filter.getStatus() != null) conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        Specification<AccountingPeriod> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<AccountingPeriod> page = accountingPeriodRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountingPeriodResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public AccountingPeriodResponse close(Long id) {
        AccountingPeriod period = find(id);
        if (period.getStatus() != AccountingPeriodStatus.OPEN) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Period is already closed");
        }
        period.setStatus(AccountingPeriodStatus.CLOSED);
        accountingPeriodRepository.save(period);
        return toResponse(period);
    }

    @Override
    @Transactional
    public AccountingPeriodResponse reopen(Long id) {
        AccountingPeriod period = find(id);
        if (period.getStatus() != AccountingPeriodStatus.CLOSED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Period is not closed");
        }
        FiscalYear fiscalYear = fiscalYearRepository.findById(period.getFiscalYearId()).orElse(null);
        if (fiscalYear != null && fiscalYear.getStatus() == FiscalYearStatus.CLOSED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot reopen a period while its fiscal year is closed — reopen the year first");
        }
        period.setStatus(AccountingPeriodStatus.OPEN);
        accountingPeriodRepository.save(period);
        return toResponse(period);
    }

    private AccountingPeriodResponse toResponse(AccountingPeriod period) {
        String fiscalYearName = fiscalYearRepository.findById(period.getFiscalYearId()).map(FiscalYear::getName).orElse(null);
        return AccountingPeriodResponse.builder()
                .id(period.getId())
                .fiscalYearId(period.getFiscalYearId())
                .fiscalYearName(fiscalYearName)
                .companyId(period.getCompanyId())
                .periodNumber(period.getPeriodNumber())
                .name(period.getName())
                .startDate(period.getStartDate())
                .endDate(period.getEndDate())
                .status(period.getStatus().name())
                .build();
    }

    private AccountingPeriod find(Long id) {
        return accountingPeriodRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Accounting period not found with id: " + id));
    }
}
