package com.example.erp.service.impl;

import com.example.erp.dto.FiscalYearFilterRequest;
import com.example.erp.dto.FiscalYearRequest;
import com.example.erp.dto.FiscalYearResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.AccountingPeriod;
import com.example.erp.entity.AccountingPeriodStatus;
import com.example.erp.entity.Company;
import com.example.erp.entity.FiscalYear;
import com.example.erp.entity.FiscalYearStatus;
import com.example.erp.exception.AppException;
import com.example.erp.repository.AccountingPeriodRepository;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.FiscalYearRepository;
import com.example.erp.service.FiscalYearService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FiscalYearServiceImpl implements FiscalYearService {

    private static final DateTimeFormatter MONTH_NAME = DateTimeFormatter.ofPattern("MMMM yyyy");

    private final FiscalYearRepository fiscalYearRepository;
    private final AccountingPeriodRepository accountingPeriodRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FiscalYearResponse> list(FiscalYearFilterRequest filter) {
        List<Specification<FiscalYear>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getStatus() != null) conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        Specification<FiscalYear> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<FiscalYear> page = fiscalYearRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public FiscalYearResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public FiscalYearResponse create(FiscalYearRequest request) {
        requireCompany(request.getCompanyId());
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }
        if (fiscalYearRepository.existsByCompanyIdAndName(request.getCompanyId(), request.getName())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A fiscal year named '" + request.getName() + "' already exists for this company");
        }

        FiscalYear fiscalYear = fiscalYearRepository.save(FiscalYear.builder()
                .companyId(request.getCompanyId())
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build());

        if (request.isGenerateMonthlyPeriods()) {
            generateMonthlyPeriods(fiscalYear);
        }
        return toResponse(fiscalYear);
    }

    @Override
    @Transactional
    public FiscalYearResponse update(Long id, FiscalYearRequest request) {
        FiscalYear fiscalYear = find(id);
        if (fiscalYear.getStatus() != FiscalYearStatus.OPEN) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only an open fiscal year can be edited");
        }
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }
        if (fiscalYearRepository.existsByCompanyIdAndName(request.getCompanyId(), request.getName())
                && !fiscalYear.getName().equals(request.getName())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A fiscal year named '" + request.getName() + "' already exists for this company");
        }

        fiscalYear.setName(request.getName());
        fiscalYear.setStartDate(request.getStartDate());
        fiscalYear.setEndDate(request.getEndDate());
        fiscalYearRepository.save(fiscalYear);
        return toResponse(fiscalYear);
    }

    @Override
    @Transactional
    public FiscalYearResponse close(Long id) {
        FiscalYear fiscalYear = find(id);
        if (fiscalYear.getStatus() != FiscalYearStatus.OPEN) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Fiscal year is already closed");
        }
        fiscalYear.setStatus(FiscalYearStatus.CLOSED);
        fiscalYearRepository.save(fiscalYear);

        for (AccountingPeriod period : accountingPeriodRepository.findByFiscalYearIdOrderByPeriodNumberAsc(id)) {
            period.setStatus(AccountingPeriodStatus.CLOSED);
            accountingPeriodRepository.save(period);
        }
        return toResponse(fiscalYear);
    }

    @Override
    @Transactional
    public FiscalYearResponse reopen(Long id) {
        FiscalYear fiscalYear = find(id);
        if (fiscalYear.getStatus() != FiscalYearStatus.CLOSED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Fiscal year is not closed");
        }
        fiscalYear.setStatus(FiscalYearStatus.OPEN);
        fiscalYearRepository.save(fiscalYear);

        for (AccountingPeriod period : accountingPeriodRepository.findByFiscalYearIdOrderByPeriodNumberAsc(id)) {
            period.setStatus(AccountingPeriodStatus.OPEN);
            accountingPeriodRepository.save(period);
        }
        return toResponse(fiscalYear);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        FiscalYear fiscalYear = find(id);
        if (fiscalYear.getStatus() == FiscalYearStatus.CLOSED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a closed fiscal year — reopen it first");
        }
        accountingPeriodRepository.deleteByFiscalYearId(id);
        fiscalYearRepository.delete(fiscalYear);
    }

    private void generateMonthlyPeriods(FiscalYear fiscalYear) {
        LocalDate cursor = fiscalYear.getStartDate().withDayOfMonth(1);
        int number = 1;
        while (!cursor.isAfter(fiscalYear.getEndDate())) {
            LocalDate periodStart = number == 1 ? fiscalYear.getStartDate() : cursor;
            LocalDate monthEnd = cursor.withDayOfMonth(cursor.lengthOfMonth());
            LocalDate periodEnd = monthEnd.isAfter(fiscalYear.getEndDate()) ? fiscalYear.getEndDate() : monthEnd;

            accountingPeriodRepository.save(AccountingPeriod.builder()
                    .fiscalYearId(fiscalYear.getId())
                    .companyId(fiscalYear.getCompanyId())
                    .periodNumber(number)
                    .name(cursor.format(MONTH_NAME))
                    .startDate(periodStart)
                    .endDate(periodEnd)
                    .build());

            cursor = cursor.plusMonths(1);
            number++;
        }
    }

    private FiscalYearResponse toResponse(FiscalYear fiscalYear) {
        Company company = companyRepository.findById(fiscalYear.getCompanyId()).orElse(null);
        List<AccountingPeriod> periods = accountingPeriodRepository.findByFiscalYearIdOrderByPeriodNumberAsc(fiscalYear.getId());
        long openCount = periods.stream().filter(p -> p.getStatus() == AccountingPeriodStatus.OPEN).count();

        return FiscalYearResponse.builder()
                .id(fiscalYear.getId())
                .companyId(fiscalYear.getCompanyId())
                .companyName(company == null ? null : company.getName())
                .name(fiscalYear.getName())
                .startDate(fiscalYear.getStartDate())
                .endDate(fiscalYear.getEndDate())
                .status(fiscalYear.getStatus().name())
                .periodCount(periods.size())
                .openPeriodCount((int) openCount)
                .build();
    }

    private FiscalYear find(Long id) {
        return fiscalYearRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Fiscal year not found with id: " + id));
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }
}
